package de.wirezat.realisticores.press;

import de.wirezat.realisticores.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Menu of the crystal press: three input slots, one output slot and the player inventory. */
public class CrystalPressMenu extends AbstractContainerMenu {
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
                new SimpleContainerData(4));
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
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.MATERIAL_SLOT, 56, 17));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.CATALYST_SLOT, 26, 17));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.FUEL_SLOT, 56, 53));
        addSlot(new SlotItemHandler(press.inventory(), CrystalPressBlockEntity.OUTPUT_SLOT, 116, 35));
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
     * Gives the remaining burn time scaled to the flame sprite.
     *
     * @return flame height in pixels, 0 to 13
     */
    public int litProgress() {
        int duration = data.get(1) == 0 ? 200 : data.get(1);
        return data.get(0) * 13 / duration;
    }

    /**
     * Gives the recipe progress scaled to the arrow sprite.
     *
     * @return arrow width in pixels, 0 to 24
     */
    public int arrowProgress() {
        int total = data.get(3);
        return total == 0 ? 0 : data.get(2) * 24 / total;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getBurnTime(RecipeType.SMELTING) > 0
                && moveItemStackTo(stack, CrystalPressBlockEntity.FUEL_SLOT, CrystalPressBlockEntity.FUEL_SLOT + 1,
                        false)) {
            slot.setChanged();
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
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
