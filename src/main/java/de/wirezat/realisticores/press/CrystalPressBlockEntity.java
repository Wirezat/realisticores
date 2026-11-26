package de.wirezat.realisticores.press;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Block entity of the crystal press: five slots, fuel burning, the press cycle and the seed crystal at work. */
public class CrystalPressBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATERIAL_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
    public static final int SEED_SLOT = 4;
    public static final int SLOTS = 5;
    public static final int DATA_VALUES = 6;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case MATERIAL_SLOT -> acceptsMaterial(stack);
                case CATALYST_SLOT -> acceptsCatalyst(stack);
                case FUEL_SLOT -> stack.getBurnTime(RecipeType.SMELTING) > 0;
                case SEED_SLOT -> PressSeed.of(stack) != null;
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final PressCycle cycle = new PressCycle();
    private final SeedState seeds = new SeedState();
    private final RecipeManager.CachedCheck<PressInput, PressRecipe> recipeCheck =
            RecipeManager.createCheck(ModPress.PRESS_TYPE.get());

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cycle.litTime();
                case 1 -> cycle.litDuration();
                case 2 -> cycle.progress();
                case 3 -> cycle.total();
                case 4 -> seeds.left();
                default -> seeds.seed().uses();
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_VALUES;
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
     * Gives the inventory: material, catalyst, fuel, output, seed crystal.
     *
     * @return the item handler
     */
    public ItemStackHandler inventory() {
        return inventory;
    }

    /**
     * Tells whether a stack is the material of any press recipe.
     *
     * @param stack the stack
     * @return true if a recipe takes it as material
     */
    private boolean acceptsMaterial(ItemStack stack) {
        return level != null
                && PressIngredients.isMaterial(level.getRecipeManager().getRecipes(), level.isClientSide, stack);
    }

    /**
     * Tells whether a stack is the catalyst of any press recipe.
     *
     * @param stack the stack
     * @return true if a recipe takes it as catalyst
     */
    private boolean acceptsCatalyst(ItemStack stack) {
        return level != null
                && PressIngredients.isCatalyst(level.getRecipeManager().getRecipes(), level.isClientSide, stack);
    }

    /**
     * Finds the recipe matching the material and catalyst slots.
     *
     * @param level the world
     * @return the recipe, empty if none matches
     */
    private Optional<RecipeHolder<PressRecipe>> currentRecipe(Level level) {
        PressInput input = new PressInput(inventory.getStackInSlot(MATERIAL_SLOT),
                inventory.getStackInSlot(CATALYST_SLOT));
        return recipeCheck.getRecipeFor(input, level);
    }

    /**
     * Tells whether the result of a run fits into the output slot.
     *
     * @param recipe the recipe
     * @return true if the slot is empty or can take the crystals of the run
     */
    private boolean outputFits(PressRecipe recipe) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, recipe.result())
                && output.getCount() + seeds.crystals(recipe.result().getCount()) <= output.getMaxStackSize();
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
        ItemStack fuel = press.inventory.getStackInSlot(FUEL_SLOT);
        int burnTime = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        if (recipe.isPresent() && press.outputFits(recipe.get().value()) && (press.cycle.isLit() || burnTime > 0)) {
            press.loadSeed();
        }
        boolean canWork = recipe.isPresent() && press.outputFits(recipe.get().value());
        boolean wasLit = press.cycle.isLit();
        PressCycle.Result result = press.cycle.tick(canWork, burnTime,
                press.seeds.time(recipe.map(r -> r.value().time()).orElse(1)));
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
        if (result.burnFuel() || result.craft() || wasLit != press.cycle.isLit()) {
            press.setChanged();
        }
    }

    /**
     * Puts a fresh seed crystal to work between two runs once the old one is worn out.
     */
    private void loadSeed() {
        PressSeed seed = PressSeed.of(inventory.getStackInSlot(SEED_SLOT));
        if (seed != null && seeds.wantsSeed(cycle.progress())) {
            inventory.extractItem(SEED_SLOT, 1, false);
            seeds.load(seed);
        }
    }

    /**
     * Finishes a run: consumes material and catalyst and adds the crystals to the output.
     *
     * @param recipe the recipe
     */
    private void craft(PressRecipe recipe) {
        inventory.getStackInSlot(MATERIAL_SLOT).shrink(recipe.materialCount());
        inventory.getStackInSlot(CATALYST_SLOT).shrink(1);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        int count = seeds.finishRun(recipe.result().getCount());
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, recipe.result().copyWithCount(count));
        } else {
            output.grow(count);
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
        tag.putInt("SeedLeft", seeds.left());
        tag.putDouble("SeedRemainder", seeds.remainder());
        PressSeed.CODEC.encodeStart(NbtOps.INSTANCE, seeds.seed()).ifSuccess(seed -> tag.put("Seed", seed));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag items = tag.getCompound("Inventory").copy();
        items.remove("Size");
        inventory.deserializeNBT(registries, items);
        PressSeed.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("Seed"))
                .ifSuccess(seed -> seeds.restore(seed, tag.getInt("SeedLeft"), tag.getDouble("SeedRemainder")));
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
