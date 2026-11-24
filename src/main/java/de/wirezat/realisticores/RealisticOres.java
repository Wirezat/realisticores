package de.wirezat.realisticores;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(RealisticOres.MOD_ID)
public class RealisticOres {
    public static final String MOD_ID = "realisticores";

    public RealisticOres(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, OreConfig.SPEC);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                KimberliteCommand.register(event.getDispatcher()));
    }
}
