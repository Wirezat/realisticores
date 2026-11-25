package de.wirezat.realisticores.press;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.RealisticOres;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Menu of the crystal press: four input slots, one output slot and the player inventory. */
public class CrystalPressMenu extends AbstractContainerMenu {
    private static final ResourceLocation MATERIAL_ICON =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "item/empty_slot_graphite");
    private static final ResourceLocation CATALYST_ICON =
            ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");
    private static final ResourceLocation FUEL_ICON =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "item/empty_slot_fuel");
    private static final ResourceLocation SEED_ICON =
            ResourceLocation.withDefaultNamespace("item/empty_slot_diamond");

    private final CrystalPressBlockEntity press;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /**
     * Creates the menu on the client from the packet sent when the press is opened.
     *
     * @param id window id
     * @param inventory player inventory
     * @param buffer packet data holding the press position
     */
    public CrystalPressMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (CrystalPressBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()),
                new SimpleContainerData(CrystalPressBlockEntity.DATA_VALUES));
    }

    /**
     * Creates the menu on the server.
     *
     * @param id window id
     * @param inventory player inventory
     * @param press the press block entity
     * @param data synchronized burn and progress values
     */
    public CrystalPressMenu(int id, Inventory inventory, CrystalPressBlockEntity press, ContainerData data) {
        super(ModPress.PRESS_MENU.get(), id);
        this.press = press;
        this.data = data;
        this.access = ContainerLevelAccess.create(press.getLevel(), press.getBlockPos());
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.MATERIAL_SLOT, 43, 18)
                .setBackground(InventoryMenu.BLOCK_ATLAS, MATERIAL_ICON));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.CATALYST_SLOT, 17, 18)
                .setBackground(InventoryMenu.BLOCK_ATLAS, CATALYST_ICON));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.FUEL_SLOT, 43, 52)
                .setBackground(InventoryMenu.BLOCK_ATLAS, FUEL_ICON));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.OUTPUT_SLOT, 144, 35));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.SEED_SLOT, 17, 52)
                .setBackground(InventoryMenu.BLOCK_ATLAS, SEED_ICON));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    /**
     * Tells whether the press is burning.
     *
     * @return true while fuel burns
     */
    public boolean isLit() {
        return data.get(0) > 0;
    }

    /**
     * Tells whether a run is under way.
     *
     * @return true while there is progress
     */
    public boolean isPressing() {
        return data.get(2) > 0;
    }

    /**
     * Gives the remaining burn time scaled to the flame sprite.
     *
     * @param height height of the full flame in pixels
     * @return flame height in pixels, at least 1 while fuel burns
     */
    public int litProgress(int height) {
        int duration = data.get(1) == 0 ? 200 : data.get(1);
        return Math.ceilDiv(data.get(0) * height, duration);
    }

    /**
     * Gives the progress of the run scaled to a length.
     *
     * @param length length of the full progress
     * @return progress from 0 to the length
     */
    public int arrowProgress(int length) {
        int total = data.get(3);
        return total == 0 ? 0 : data.get(2) * length / total;
    }

    /**
     * Gives how much of the seed crystal at work is left, scaled to its gauge.
     *
     * @param height height of a full gauge in pixels
     * @return gauge height in pixels, 0 without a seed
     */
    public int seedProgress(int height) {
        int uses = data.get(5);
        return uses == 0 ? 0 : Math.ceilDiv(data.get(4) * height, uses);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < CrystalPressBlockEntity.SLOTS) {
            if (!moveItemStackTo(stack, CrystalPressBlockEntity.SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getBurnTime(RecipeType.SMELTING) > 0
                && moveItemStackTo(stack, CrystalPressBlockEntity.FUEL_SLOT, CrystalPressBlockEntity.FUEL_SLOT + 1,
                        false)) {
            slot.setChanged();
        } else if (!moveItemStackTo(stack, 0, 2, false)
                && !moveItemStackTo(stack, CrystalPressBlockEntity.SEED_SLOT, CrystalPressBlockEntity.SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CRYSTAL_PRESS.get());
    }
}
