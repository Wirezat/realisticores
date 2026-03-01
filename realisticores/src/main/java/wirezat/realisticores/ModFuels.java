package wirezat.realisticores;

import net.fabricmc.fabric.api.registry.FuelRegistry;

public class ModFuels {

    public static void register() {
        FuelRegistry.INSTANCE.add(ModItems.LIGNIT,        200);
        FuelRegistry.INSTANCE.add(ModItems.COKE,          600);
        FuelRegistry.INSTANCE.add(ModBlocks.LIGNIT_BLOCK, 1800);
        FuelRegistry.INSTANCE.add(ModBlocks.COKE_BLOCK,   5400);
    }
}