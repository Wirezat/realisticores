package de.wirezat.realisticores.kimberlite;

/** Shape of the crater bowl and the level of the crater lake. */
public final class Crater {
    private final KimberliteParams.CraterParams params;

    /**
     * Creates a crater model.
     *
     * @param params bowl settings
     */
    public Crater(KimberliteParams.CraterParams params) {
        this.params = params;
    }

    /**
     * Gives the bowl depth of a pipe.
     *
     * @param pipe the pipe
     * @return depth in blocks between rim and bowl centre
     */
    public int depth(Pipe pipe) {
        long depth = Math.round(pipe.diameter() * params.depthPerDiameter());
        return params.depth().clamp((int) depth);
    }

    /**
     * Computes the bowl floor height of a column.
     *
     * @param pipe the pipe
     * @param wallDistance distance from the axis relative to the wall (0 axis, 1 wall)
     * @param rimY mean terrain height around the crater
     * @param surfaceY terrain height of the column
     * @return floor Y: rimY minus the bowl depth at the axis, rimY at the wall; never deeper than the allowed
     *         wall height below the terrain and never above the terrain
     */
    public int floorY(Pipe pipe, double wallDistance, int rimY, int surfaceY) {
        double d = Math.min(1.0, Math.max(0.0, wallDistance));
        int bowl = rimY - (int) Math.round(depth(pipe) * (1.0 - d * d));
        int deepestAllowed = surfaceY - Math.max(params.maxWallHeight(), depth(pipe));
        return Math.min(surfaceY, Math.max(bowl, deepestAllowed));
    }

    /**
     * Computes the lake surface height.
     *
     * @param lowestRimY lowest terrain height around the crater
     * @param seaLevel sea level of the world
     * @return Y of the top water block: one block below the lowest rim point, but at least the top water block of the sea (one below sea level)
     */
    public int waterLevel(int lowestRimY, int seaLevel) {
        return Math.max(lowestRimY - 1, seaLevel - 1);
    }

    /**
     * Decides whether a crater holds a lake.
     *
     * @param wetBiome true if the biome at the crater has precipitation
     * @return true if the crater fills with water
     */
    public boolean hasLake(boolean wetBiome) {
        return wetBiome || params.lakeInDryBiomes();
    }
}
