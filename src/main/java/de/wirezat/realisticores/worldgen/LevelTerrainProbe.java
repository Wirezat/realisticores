package de.wirezat.realisticores.worldgen;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

/** Reads the original terrain of a loaded world from its chunk generator. */
public final class LevelTerrainProbe implements TerrainProbe {
    private final NoiseBasedChunkGenerator generator;
    private final LevelHeightAccessor heights;
    private final RandomState random;

    /**
     * Creates a probe.
     *
     * @param generator noise chunk generator of the world
     * @param heights height range of the world
     * @param random random state of the generator
     */
    public LevelTerrainProbe(NoiseBasedChunkGenerator generator, LevelHeightAccessor heights, RandomState random) {
        this.generator = generator;
        this.heights = heights;
        this.random = random;
    }

    /**
     * Creates a probe for a world whose terrain comes from a noise chunk generator.
     *
     * @param level the world to read
     * @return the probe, empty for worlds with another generator, such as superflat
     */
    public static Optional<TerrainProbe> of(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator
                ? Optional.of(new LevelTerrainProbe(generator, level, level.getChunkSource().randomState()))
                : Optional.empty();
    }

    @Override
    public int terrainHeight(int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, random) - 1;
    }

    @Override
    public Climate climate(int x, int z) {
        return Climate.of(biomeAt(x, terrainHeight(x, z), z));
    }

    @Override
    public boolean hasPrecipitation(int x, int y, int z) {
        return biomeAt(x, y, z).value().hasPrecipitation();
    }

    /**
     * Reads the biome the generator places at a position.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return the biome
     */
    private Holder<Biome> biomeAt(int x, int y, int z) {
        return generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), random.sampler());
    }
}
