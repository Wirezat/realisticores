package de.wirezat.realisticores.press;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
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
}
