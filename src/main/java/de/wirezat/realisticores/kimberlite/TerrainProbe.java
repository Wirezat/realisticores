package de.wirezat.realisticores.kimberlite;

/** Reads the terrain and climate of a world position without generating chunks. */
public interface TerrainProbe {
    /**
     * Gives the original terrain height.
     *
     * @param x world X
     * @param z world Z
     * @return Y of the top solid block
     */
    int terrainHeight(int x, int z);

    /**
     * Classifies the climate at a position.
     *
     * @param x world X
     * @param z world Z
     * @return climate class of the biome at the terrain surface
     */
    Climate climate(int x, int z);

    /**
     * Tells whether the biome at a position has precipitation.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return true for biomes with rain or snow, false for dry biomes
     */
    boolean hasPrecipitation(int x, int y, int z);
}
