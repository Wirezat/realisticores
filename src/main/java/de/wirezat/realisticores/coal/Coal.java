package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.kimberlite.TerrainProbe;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

/** Shared generator objects of coal basin generation, rebuilt whenever the config changes. */
public final class Coal {
    private static final Executor SURVEYOR = new ForkJoinPool(1);
    private static final int CANDIDATES_PER_RESULT = 20;

    private static volatile State state;

    private Coal() {
    }

    private record BasinKey(long seed, Basin basin) {
    }

    private record State(CoalParams params, BasinGenerator generator, SeamModel model,
            ConcurrentMap<BasinKey, Optional<ResolvedBasin>> basins) {
    }

    private static State state() {
        State current = state;
        if (current == null) {
            CoalParams params = CoalConfig.params();
            current = new State(params, new BasinGenerator(params), new SeamModel(params), new ConcurrentHashMap<>());
            state = current;
        }
        return current;
    }

    /** Drops the cached objects and measured basins so the next use rebuilds them. */
    public static void invalidate() {
        state = null;
    }

    /**
     * Returns the shared layer model.
     *
     * @return model for the active parameters
     */
    public static SeamModel model() {
        return state().model();
    }

    /**
     * Lists the basins with coal that may touch an area, measuring the terrain of each one on first use.
     *
     * @param seed world seed
     * @param probe terrain source
     * @param ranks ranks that have a block
     * @param minX west edge of the area in blocks
     * @param minZ north edge of the area in blocks
     * @param maxX east edge of the area in blocks
     * @param maxZ south edge of the area in blocks
     * @return basins outside high mountains that hold at least one of the ranks
     */
    public static List<ResolvedBasin> basinsNear(long seed, TerrainProbe probe, Set<Rank> ranks, int minX, int minZ,
            int maxX, int maxZ) {
        State current = state();
        return resolved(current, seed, probe, ranks,
                current.generator().basinsNear(seed, minX, minZ, maxX, maxZ).stream()).toList();
    }

    /**
     * Finds the basins with coal nearest to a position, measuring as few basins as possible.
     *
     * @param seed world seed
     * @param probe terrain source
     * @param ranks ranks that have a block
     * @param x world X
     * @param z world Z
     * @param radius search limit in blocks
     * @param count most basins to find
     * @return basins by distance of their centre
     */
    public static List<ResolvedBasin> nearest(long seed, TerrainProbe probe, Set<Rank> ranks, int x, int z, int radius,
            int count) {
        State current = state();
        Stream<Basin> candidates = current.generator()
                .basinsNear(seed, x - radius, z - radius, x + radius, z + radius).stream()
                .sorted(Comparator.comparingDouble(basin -> Math.hypot(basin.x() - x, basin.z() - z)))
                .limit((long) count * CANDIDATES_PER_RESULT);
        return resolved(current, seed, probe, ranks, candidates).limit(count).toList();
    }

    private static Stream<ResolvedBasin> resolved(State current, long seed, TerrainProbe probe, Set<Rank> ranks,
            Stream<Basin> basins) {
        return basins.flatMap(basin -> current.basins().computeIfAbsent(new BasinKey(seed, basin),
                        key -> resolve(basin, probe, current.params())).stream())
                .filter(basin -> CoalOres.generates(basin.type(), ranks));
    }

    /**
     * Gives the background thread for surveys that measure many basins.
     *
     * @return executor with one thread
     */
    public static Executor surveyor() {
        return SURVEYOR;
    }

    private static Optional<ResolvedBasin> resolve(Basin basin, TerrainProbe probe, CoalParams params) {
        BasinSite site = BasinSite.measure(basin, probe, params.relief());
        if (site.mountain(params.relief())) {
            return Optional.empty();
        }
        BasinType type = site.type(basin, params.relief());
        return Optional.of(new ResolvedBasin(basin, type, site.datum(), BasinLayout.of(basin, type, params)));
    }
}
