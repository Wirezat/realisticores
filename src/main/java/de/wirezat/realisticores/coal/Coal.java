package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.kimberlite.TerrainProbe;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/** Shared generator objects of coal basin generation, rebuilt whenever the config changes. */
public final class Coal {
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

    private static Stream<ResolvedBasin> resolved(State current, long seed, TerrainProbe probe, Set<Rank> ranks,
            Stream<Basin> basins) {
        return basins.flatMap(basin -> current.basins().computeIfAbsent(new BasinKey(seed, basin),
                        key -> resolve(basin, probe, current.params())).stream())
                .filter(basin -> CoalOres.generates(basin.type(), ranks));
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
