package de.wirezat.realisticores;

import de.wirezat.realisticores.kimberlite.Kimberlite;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(RealisticOres.MOD_ID)
public class RealisticOres {
    public static final String MOD_ID = "realisticores";

    public RealisticOres(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, OreConfig.SPEC, "realisticores/main.toml");
        container.registerConfig(ModConfig.Type.COMMON, KimberliteConfig.SPEC,
                "realisticores/kimberlite_pipe/kimberlite_pipe.toml");
        modBus.addListener((ModConfigEvent.Loading event) -> Kimberlite.invalidate());
        modBus.addListener((ModConfigEvent.Reloading event) -> Kimberlite.invalidate());
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModTabs.register(modBus);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                KimberliteCommand.register(event.getDispatcher()));
    }
}
