package wirezat.realisticores.blocks.machines.cubicpress;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.registry.DynamicRegistryManager;
import wirezat.realisticores.ImplementedInventory;
import wirezat.realisticores.ModBlocks;

import java.util.Optional;

public class CubicPressBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, ExtendedScreenHandlerFactory {

    private static final int INPUT_SLOT = 0;
    private static final int CATALYST_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int INVENTORY_SIZE = 3;

    private static final int PROPERTY_INDEX_PROCESSING = 0;
    private static final int PROPERTY_INDEX_TOTAL = 1;

    private static final String NBT_PROCESSING_TIME = "processingTime";
    private static final String NBT_TOTAL_PROCESSING_TIME = "totalProcessingTime";

    private static final int[] SLOTS_UP = new int[]{INPUT_SLOT};
    private static final int[] SLOTS_SIDE = new int[]{CATALYST_SLOT};
    private static final int[] SLOTS_DOWN = new int[]{OUTPUT_SLOT};
    private static final int[] SLOTS_NONE = new int[]{};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int processingTime = 0;
    private int totalProcessingTime = 0;

    // Property delegate used by the screen handler to sync processing progress to client
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case PROPERTY_INDEX_PROCESSING -> processingTime;
                case PROPERTY_INDEX_TOTAL -> totalProcessingTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case PROPERTY_INDEX_PROCESSING -> processingTime = value;
                case PROPERTY_INDEX_TOTAL -> totalProcessingTime = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public CubicPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CUBIC_PRESS_BLOCK_ENTITY, pos, state);
    }

    // ImplementedInventory ---------------------------------------------------------------
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    // SidedInventory ---------------------------------------------------------------------
    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == INPUT_SLOT || slot == CATALYST_SLOT;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> SLOTS_UP;
            case DOWN -> SLOTS_DOWN;
            case NORTH, SOUTH, EAST, WEST -> SLOTS_SIDE;
            default -> SLOTS_NONE;
        };
    }

    // NBT (persistence) ------------------------------------------------------------------
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        this.processingTime = nbt.getInt(NBT_PROCESSING_TIME);
        this.totalProcessingTime = nbt.getInt(NBT_TOTAL_PROCESSING_TIME);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt(NBT_PROCESSING_TIME, this.processingTime);
        nbt.putInt(NBT_TOTAL_PROCESSING_TIME, this.totalProcessingTime);
    }

    // --- Recipe / crafting helpers --------------------------------------------------------

    private static Optional<CubicPressRecipe> findMatchingRecipe(Inventory inventory, World world) {
        return world.getRecipeManager().getFirstMatch(CubicPressRecipe.TYPE, inventory, world);
    }

    /**
     * Check whether the given output stack can be placed into the output slot.
     */
    private boolean canAcceptOutput(ItemStack outputStack) {
        ItemStack existing = this.getStack(OUTPUT_SLOT);

        if (existing.isEmpty()) {
            return true;
        }

        if (existing.getItem() != outputStack.getItem()) {
            return false;
        }

        return existing.getCount() + outputStack.getCount() <= existing.getMaxCount();
    }

    /**
     * Perform the crafting operation using the provided recipe.
     * This mutates the inventory (consumes inputs) and writes the resulting output to the output slot.
     */
    private void craft(CubicPressRecipe recipe, DynamicRegistryManager registryManager) {
        // consume inputs according to recipe
        this.removeStack(INPUT_SLOT, recipe.getInputCount());

        if (!recipe.getCatalyst().isEmpty()) {
            this.removeStack(CATALYST_SLOT, recipe.getCatalystCount());
        }

        ItemStack currentOutput = this.getStack(OUTPUT_SLOT);
        ItemStack produced = recipe.getOutput(registryManager);

        if (currentOutput.isEmpty()) {
            // set a copy to avoid aliasing any recipe-owned instance
            this.setStack(OUTPUT_SLOT, produced.copy());
        } else {
            currentOutput.increment(produced.getCount());
        }
    }

    /**
     * Tick handler: simplified control flow using guard clauses and a dirty flag to avoid
     * unnecessary markDirty() calls.
     */
    public static void tick(World world, BlockPos pos, BlockState state, CubicPressBlockEntity blockEntity) {
        if (world.isClient) return; // server-side only

        boolean dirty = false;

        Optional<CubicPressRecipe> recipeOptional = findMatchingRecipe(blockEntity, world);
        if (recipeOptional.isEmpty()) {
            if (blockEntity.processingTime != 0) {
                blockEntity.processingTime = 0;
                dirty = true;
            }

            if (dirty) blockEntity.markDirty();
            return;
        }

        CubicPressRecipe recipe = recipeOptional.get();

        // Quick check whether we can put the recipe output into the output slot
        if (!blockEntity.canAcceptOutput(recipe.getOutputItem())) {
            if (blockEntity.processingTime != 0) {
                blockEntity.processingTime = 0; // reset when can't output
                dirty = true;
            }

            if (dirty) blockEntity.markDirty();
            return;
        }

        // start processing if not already started
        if (blockEntity.processingTime == 0) {
            blockEntity.totalProcessingTime = recipe.getProcessingTime();
            dirty = true;
        }

        blockEntity.processingTime++;
        dirty = true;

        // finished
        if (blockEntity.processingTime >= blockEntity.totalProcessingTime) {
            blockEntity.craft(recipe, world.getRegistryManager());
            blockEntity.processingTime = 0;
            dirty = true;
        }

        if (dirty) blockEntity.markDirty();
    }

    // --- UI / ScreenHandler ---------------------------------------------------------------
    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CubicPressScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
}
