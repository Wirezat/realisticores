package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.indicator.Plume;
import de.wirezat.realisticores.indicator.PlumePath;
import de.wirezat.realisticores.indicator.Plumes;
import de.wirezat.realisticores.worldgen.Memo;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Shared generator objects of porphyry copper generation, rebuilt whenever the config changes. */
public final class Porphyry {
    private static final int CACHE_LIMIT = 16384;
    private static final int HEIGHT_GRID = 32;

    private static volatile State state;

    private Porphyry() {
    }

    private record State(PorphyryParams params, ClusterGenerator generator, PorphyryShape shape,
            Memo<GridKey, Integer> heights, Plumes<Body> plumes) {
    }

    private record GridKey(long seed, int x, int z) {
    }

    /**
     * Gives the shared generator objects, building them from the config on first use.
     *
     * @return the current state
     */
    private static State state() {
        State current = state;
        if (current == null) {
            PorphyryParams params = PorphyryConfig.params();
            ClusterGenerator generator = new ClusterGenerator(params);
            current = new State(params, generator, new PorphyryShape(), new Memo<>(CACHE_LIMIT),
                    new Plumes<>(params.plume(), params.cellSize(), generator::bodiesNear));
            state = current;
        }
        return current;
    }

    /** Drops the cached objects and measured heights so the next use rebuilds them. */
    public static void invalidate() {
        state = null;
    }

    /**
     * Returns the shared group generator.
     *
     * @return generator for the active parameters
     */
    public static ClusterGenerator generator() {
        return state().generator();
    }

    /**
     * Returns the shared shell model.
     *
     * @return the shell model
     */
    public static PorphyryShape shape() {
        return state().shape();
    }

    /**
     * Returns the active parameters.
     *
     * @return porphyry generation parameters
     */
    public static PorphyryParams params() {
        return state().params();
    }

    /**
     * Collects the indicator trails that can reach an area, tracing missing ones on the trail pool.
     *
     * @param seed world seed
     * @param probe terrain and climate source, read from the pool's threads
     * @param seaLevel Y of the sea surface
     * @param minX west edge of the area in blocks
     * @param minZ north edge of the area in blocks
     * @param maxX east edge of the area in blocks
     * @param maxZ south edge of the area in blocks
     * @return future of the trails of all deposits close enough to the area
     */
    public static CompletableFuture<List<PlumePath<Body>>> plumesReaching(long seed, TerrainProbe probe,
            int seaLevel, int minX, int minZ, int maxX, int maxZ) {
        return state().plumes().reaching(seed, probe, seaLevel, minX, minZ, maxX, maxZ);
    }

    /**
     * Picks a trail point for a map on the trail pool, away from every deposit if possible.
     *
     * @param seed world seed
     * @param probe terrain and climate source, read from the pool's threads
     * @param seaLevel Y of the sea surface
     * @param bodies deposits to try, in this order
     * @param pick random number that selects one of the suitable points
     * @return future of the point, empty if no trail has one
     */
    public static CompletableFuture<Optional<Plume.Spot>> mapSpot(long seed, TerrainProbe probe, int seaLevel,
            List<Body> bodies, int pick) {
        return state().plumes().mapSpot(seed, probe, seaLevel, bodies, pick, body -> true);
    }

    /**
     * Gives the terrain height smoothed over a coarse grid, so the water table runs calmer than the surface.
     *
     * @param seed world seed
     * @param probe reader for the original terrain
     * @param x world X
     * @param z world Z
     * @return terrain height interpolated between the grid points around the position
     */
    public static double smoothHeight(long seed, TerrainProbe probe, int x, int z) {
        Memo<GridKey, Integer> heights = state().heights();
        int gx = Math.floorDiv(x, HEIGHT_GRID);
        int gz = Math.floorDiv(z, HEIGHT_GRID);
        double tx = (x - gx * HEIGHT_GRID) / (double) HEIGHT_GRID;
        double tz = (z - gz * HEIGHT_GRID) / (double) HEIGHT_GRID;
        double[] corner = new double[4];
        for (int i = 0; i < 4; i++) {
            int cx = (gx + i % 2) * HEIGHT_GRID;
            int cz = (gz + i / 2) * HEIGHT_GRID;
            corner[i] = heights.get(new GridKey(seed, cx, cz), key -> probe.terrainHeight(cx, cz));
        }
        double north = corner[0] + (corner[1] - corner[0]) * tx;
        double south = corner[2] + (corner[3] - corner[2]) * tx;
        return north + (south - north) * tz;
    }
}
