package de.wirezat.realisticores;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(RealisticOres.MOD_ID)
public class RealisticOres {
    public static final String MOD_ID = "realisticores";

    public RealisticOres(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, OreConfig.SPEC);
    }
}
