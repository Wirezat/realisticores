package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.kimberlite.Climate;
import de.wirezat.realisticores.kimberlite.TerrainProbe;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

/** Reads the original terrain of a loaded world from its chunk generator. */
public final class LevelTerrainProbe implements TerrainProbe {
    private final ServerLevel level;
    private final NoiseBasedChunkGenerator generator;

    /**
     * Creates a probe.
     *
     * @param level the world to read
     * @param generator its noise chunk generator
     */
    public LevelTerrainProbe(ServerLevel level, NoiseBasedChunkGenerator generator) {
        this.level = level;
        this.generator = generator;
    }

    @Override
    public int terrainHeight(int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level,
                level.getChunkSource().randomState()) - 1;
    }

    @Override
    public Climate climate(int x, int z) {
        return Climate.of(biomeAt(x, terrainHeight(x, z), z));
    }

    @Override
    public boolean hasPrecipitation(int x, int y, int z) {
        return biomeAt(x, y, z).value().hasPrecipitation();
    }

    private Holder<Biome> biomeAt(int x, int y, int z) {
        return generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), level.getChunkSource().randomState().sampler());
    }
}
