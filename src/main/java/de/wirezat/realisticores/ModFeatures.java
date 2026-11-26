package de.wirezat.realisticores;

import de.wirezat.realisticores.coal.LooseCoalFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's world generation features. */
public final class ModFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, RealisticOres.MOD_ID);

    static {
        FEATURES.register("loose_coal", LooseCoalFeature::new);
    }

    private ModFeatures() {
    }

    /**
     * Registers the features on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
