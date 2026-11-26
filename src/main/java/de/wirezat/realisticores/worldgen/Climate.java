package de.wirezat.realisticores.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/** Climate classes that set how far a pipe spreads its indicator minerals. */
public enum Climate {
    COLD,
    DRY,
    TEMPERATE,
    OCEAN;

    private static final float COLD_BELOW = 0.3F;

    /**
     * Classifies a biome.
     *
     * @param biome the biome
     * @return OCEAN for oceans, COLD below the cold temperature limit, DRY without precipitation, else TEMPERATE
     */
    public static Climate of(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_OCEAN)) {
            return OCEAN;
        }
        Biome value = biome.value();
        if (value.getBaseTemperature() < COLD_BELOW) {
            return COLD;
        }
        return value.hasPrecipitation() ? TEMPERATE : DRY;
    }
}
