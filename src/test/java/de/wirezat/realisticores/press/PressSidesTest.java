package de.wirezat.realisticores.press;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

class PressSidesTest {
    @Test
    void facesMapToSlotsRelativeToFront() {
        Direction facing = Direction.NORTH;
        assertEquals(new PressSides.Access(CrystalPressBlockEntity.MATERIAL_SLOT, true, false),
                PressSides.forSide(Direction.UP, facing));
        assertEquals(new PressSides.Access(CrystalPressBlockEntity.OUTPUT_SLOT, false, true),
                PressSides.forSide(Direction.DOWN, facing));
        assertEquals(new PressSides.Access(CrystalPressBlockEntity.FUEL_SLOT, true, false),
                PressSides.forSide(Direction.SOUTH, facing));
        assertEquals(new PressSides.Access(CrystalPressBlockEntity.CATALYST_SLOT, true, false),
                PressSides.forSide(Direction.EAST, facing));
        assertEquals(new PressSides.Access(CrystalPressBlockEntity.CATALYST_SLOT, true, false),
                PressSides.forSide(Direction.WEST, facing));
    }

    @Test
    void bottomYieldsSpentFuelContainersButNotFuel() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ItemStackHandler inventory = new ItemStackHandler(4) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == CrystalPressBlockEntity.FUEL_SLOT && stack.is(Items.LAVA_BUCKET);
            }
        };
        IItemHandler bottom = PressSides.handlerFor(inventory, Direction.DOWN, Direction.NORTH);
        inventory.setStackInSlot(CrystalPressBlockEntity.FUEL_SLOT, new ItemStack(Items.LAVA_BUCKET));
        assertTrue(bottom.extractItem(0, 1, true).isEmpty());
        inventory.setStackInSlot(CrystalPressBlockEntity.FUEL_SLOT, new ItemStack(Items.BUCKET));
        assertTrue(bottom.extractItem(0, 1, true).is(Items.BUCKET));
        inventory.setStackInSlot(CrystalPressBlockEntity.OUTPUT_SLOT, new ItemStack(Items.DIAMOND));
        assertTrue(bottom.extractItem(1, 1, true).is(Items.DIAMOND));
    }
}
