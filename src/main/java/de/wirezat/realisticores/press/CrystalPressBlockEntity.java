package de.wirezat.realisticores.press;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Block entity of the crystal press: four slots, fuel burning and the press cycle. */
public class CrystalPressBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATERIAL_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case MATERIAL_SLOT -> acceptsMaterial(stack);
                case CATALYST_SLOT -> acceptsCatalyst(stack);
                case FUEL_SLOT -> stack.getBurnTime(RecipeType.SMELTING) > 0;
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final PressCycle cycle = new PressCycle();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cycle.litTime();
                case 1 -> cycle.litDuration();
                case 2 -> cycle.progress();
                default -> cycle.total();
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    /**
     * Creates the block entity.
     *
     * @param pos position of the press
     * @param state block state of the press
     */
    public CrystalPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModPress.PRESS_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Gives the inventory: material, catalyst, fuel, output.
     *
     * @return the item handler
     */
    public ItemStackHandler inventory() {
        return inventory;
    }

    private boolean acceptsMaterial(ItemStack stack) {
        return level != null && level.getRecipeManager().getAllRecipesFor(ModPress.PRESS_TYPE.get()).stream()
                .anyMatch(holder -> holder.value().material().test(stack));
    }

    private boolean acceptsCatalyst(ItemStack stack) {
        return level != null && level.getRecipeManager().getAllRecipesFor(ModPress.PRESS_TYPE.get()).stream()
                .anyMatch(holder -> holder.value().catalyst().test(stack));
    }

    private Optional<RecipeHolder<PressRecipe>> currentRecipe(Level level) {
        PressInput input = new PressInput(inventory.getStackInSlot(MATERIAL_SLOT),
                inventory.getStackInSlot(CATALYST_SLOT));
        return level.getRecipeManager().getRecipeFor(ModPress.PRESS_TYPE.get(), input, level);
    }

    private boolean outputFits(PressRecipe recipe) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, recipe.result())
                && output.getCount() + recipe.result().getCount() <= output.getMaxStackSize();
    }

    /**
     * Advances the press by one tick on the server.
     *
     * @param level the world
     * @param pos position of the press
     * @param state block state of the press
     * @param press the block entity
     */
    public static void tick(Level level, BlockPos pos, BlockState state, CrystalPressBlockEntity press) {
        if (level.isClientSide) {
            return;
        }
        Optional<RecipeHolder<PressRecipe>> recipe = press.currentRecipe(level);
        boolean canWork = recipe.isPresent() && press.outputFits(recipe.get().value());
        ItemStack fuel = press.inventory.getStackInSlot(FUEL_SLOT);
        int burnTime = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        boolean wasLit = press.cycle.isLit();
        PressCycle.Result result = press.cycle.tick(canWork, burnTime, recipe.map(r -> r.value().time()).orElse(1));
        if (result.burnFuel()) {
            ItemStack remainder = fuel.getCraftingRemainingItem();
            fuel.shrink(1);
            if (fuel.isEmpty()) {
                press.inventory.setStackInSlot(FUEL_SLOT, remainder);
            }
        }
        if (result.craft() && recipe.isPresent()) {
            press.craft(recipe.get().value());
        }
        if (wasLit != press.cycle.isLit()) {
            level.setBlock(pos, state.setValue(CrystalPressBlock.LIT, press.cycle.isLit()), 3);
        }
        press.setChanged();
    }

    private void craft(PressRecipe recipe) {
        inventory.getStackInSlot(MATERIAL_SLOT).shrink(recipe.materialCount());
        inventory.getStackInSlot(CATALYST_SLOT).shrink(1);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, recipe.result().copy());
        } else {
            output.grow(recipe.result().getCount());
        }
    }

    /** Drops the contents of all slots at the block position. */
    public void dropContents() {
        if (level == null) {
            return;
        }
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            container.setItem(slot, inventory.getStackInSlot(slot));
        }
        Containers.dropContents(level, worldPosition, container);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LitTime", cycle.litTime());
        tag.putInt("LitDuration", cycle.litDuration());
        tag.putInt("Progress", cycle.progress());
        tag.putInt("Total", cycle.total());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        cycle.restore(tag.getInt("LitTime"), tag.getInt("LitDuration"), tag.getInt("Progress"), tag.getInt("Total"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.realisticores.crystal_press");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CrystalPressMenu(id, playerInventory, this, data);
    }
}
