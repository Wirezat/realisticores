package de.wirezat.realisticores;

import de.wirezat.realisticores.coal.PlacePieces;
import de.wirezat.realisticores.indicator.ModPan;
import de.wirezat.realisticores.indicator.TrailEffects;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.Deposits;
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

    /**
     * Registers configs, registries and event listeners of the mod.
     *
     * @param modBus the mod event bus
     * @param container the mod container
     */
    public RealisticOres(IEventBus modBus, ModContainer container) {
        Deposits.validate(OreConfig.registry(), Deposits.ALL);
        container.registerConfig(ModConfig.Type.COMMON, OreConfig.SPEC, "realisticores/main.toml");
        for (DepositType deposit : Deposits.ALL) {
            container.registerConfig(ModConfig.Type.COMMON, deposit.spec(),
                    "realisticores/" + deposit.id() + "/" + deposit.id() + ".toml");
        }
        modBus.addListener((ModConfigEvent.Loading event) -> invalidateConfig());
        modBus.addListener((ModConfigEvent.Reloading event) -> invalidateConfig());
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModTabs.register(modBus);
        ModFeatures.register(modBus);
        ModPress.register(modBus);
        ModPan.register(modBus);
        NeoForge.EVENT_BUS.addListener(TrailEffects::tick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> invalidateConfig());
        NeoForge.EVENT_BUS.addListener(PlacePieces::onUse);
        NeoForge.EVENT_BUS.addListener(SurveyEvents::onTrades);
        NeoForge.EVENT_BUS.addListener(NaturalDiamondTrades::onTrades);
        NeoForge.EVENT_BUS.addListener(SurveyEvents::onLoot);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                LocateDepositCommand.register(event.getDispatcher()));
    }

    /**
     * Drops the resolved config and the cached generator objects of every deposit.
     */
    private static void invalidateConfig() {
        OreConfig.invalidate();
        Deposits.ALL.forEach(DepositType::invalidate);
    }
}
