package de.wirezat.realisticores.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.junit.jupiter.api.Test;

class PanWaterTest {
    @Test
    void panTakesOnlyWaterUpToOneBucket() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ModPan.PanFluidHandler handler = new ModPan.PanFluidHandler(new ItemStack(Items.STICK));
        assertEquals(0, handler.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE));
        assertEquals(1000, handler.fill(new FluidStack(Fluids.WATER, 1500), FluidAction.EXECUTE));
    }
}
