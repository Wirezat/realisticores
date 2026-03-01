package wirezat.realisticores;

import net.fabricmc.fabric.api.registry.FuelRegistry;

public class ModFuels {

    public static void register() {
        FuelRegistry.INSTANCE.add(ModItems.LIGNIT,        400);
        FuelRegistry.INSTANCE.add(ModItems.COKE,          1200);
        FuelRegistry.INSTANCE.add(ModBlocks.LIGNIT_BLOCK, 3600);
        FuelRegistry.INSTANCE.add(ModBlocks.COKE_BLOCK,   10800);
    }
}