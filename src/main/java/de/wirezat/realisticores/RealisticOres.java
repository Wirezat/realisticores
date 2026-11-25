package de.wirezat.realisticores;

import de.wirezat.realisticores.coal.Coal;
import de.wirezat.realisticores.coal.CoalConfig;
import de.wirezat.realisticores.indicator.ModPan;
import de.wirezat.realisticores.indicator.TrailEffects;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.press.ModPress;
import de.wirezat.realisticores.survey.SurveyEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(RealisticOres.MOD_ID)
public class RealisticOres {
    public static final String MOD_ID = "realisticores";

    public RealisticOres(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, OreConfig.SPEC, "realisticores/main.toml");
        container.registerConfig(ModConfig.Type.COMMON, KimberliteConfig.SPEC,
                "realisticores/kimberlite_pipe/kimberlite_pipe.toml");
        container.registerConfig(ModConfig.Type.COMMON, CoalConfig.SPEC, "realisticores/coal_basin/coal_basin.toml");
        modBus.addListener((ModConfigEvent.Loading event) -> invalidateConfig());
        modBus.addListener((ModConfigEvent.Reloading event) -> invalidateConfig());
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModTabs.register(modBus);
        ModPress.register(modBus);
        ModPan.register(modBus);
        NeoForge.EVENT_BUS.addListener(TrailEffects::tick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> Kimberlite.invalidate());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> Coal.invalidate());
        NeoForge.EVENT_BUS.addListener(SurveyEvents::onTrades);
        NeoForge.EVENT_BUS.addListener(NaturalDiamondTrades::onTrades);
        NeoForge.EVENT_BUS.addListener(SurveyEvents::onLoot);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                LocateDepositCommand.register(event.getDispatcher()));
    }

    private static void invalidateConfig() {
        OreConfig.invalidate();
        Kimberlite.invalidate();
        Coal.invalidate();
    }
}
