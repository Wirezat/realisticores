package de.wirezat.realisticores.press;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/** Maps the faces of the crystal press to its inventory slots for hoppers and pipes. */
public final class PressSides {
    /**
     * Access of one face to one slot.
     *
     * @param slot slot index of the press inventory
     * @param insert whether items may be inserted
     * @param extract whether items may be extracted
     */
    public record Access(int slot, boolean insert, boolean extract) {
    }

    private PressSides() {
    }

    /**
     * Gives the access of a face: top feeds material, bottom yields output, back feeds fuel, the other sides feed
     * the catalyst.
     *
     * @param side the face being accessed
     * @param facing the horizontal front of the press
     * @return slot and allowed directions for that face
     */
    public static Access forSide(Direction side, Direction facing) {
        if (side == Direction.UP) {
            return new Access(CrystalPressBlockEntity.MATERIAL_SLOT, true, false);
        }
        if (side == Direction.DOWN) {
            return new Access(CrystalPressBlockEntity.OUTPUT_SLOT, false, true);
        }
        if (side == facing.getOpposite()) {
            return new Access(CrystalPressBlockEntity.FUEL_SLOT, true, false);
        }
        return new Access(CrystalPressBlockEntity.CATALYST_SLOT, true, false);
    }

    /**
     * Wraps the press inventory so a face only reaches its own slot.
     *
     * @param inventory the full press inventory
     * @param side the face being accessed, or null for unrestricted access
     * @param facing the horizontal front of the press
     * @return the handler to expose on that face
     */
    public static IItemHandler handlerFor(ItemStackHandler inventory, @Nullable Direction side, Direction facing) {
        if (side == null) {
            return inventory;
        }
        Access access = forSide(side, facing);
        return new RangedWrapper(inventory, access.slot(), access.slot() + 1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return access.insert() ? super.insertItem(slot, stack, simulate) : stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return access.extract() ? super.extractItem(slot, amount, simulate)
                        : ItemStack.EMPTY;
            }
        };
    }
}
