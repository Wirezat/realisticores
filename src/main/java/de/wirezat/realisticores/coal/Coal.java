package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.worldgen.Memo;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Shared generator objects of coal basin generation, rebuilt whenever the config changes. */
public final class Coal {
    private static final int MAX_CANDIDATES = 300;
    private static final int CACHE_LIMIT = 4096;

    private static volatile State state;

    private Coal() {
    }

    private record State(CoalParams params, CoalOres.Seams seams, BasinGenerator generator, SeamModel model,
            SurfaceHint surface, Accessories accessories, Memo<Basin, Optional<ResolvedBasin>> basins) {
    }

    private static State state() {
        State current = state;
        if (current == null) {
            CoalParams params = CoalConfig.params();
            current = new State(params, CoalOres.Seams.of(OreConfig.resolved().minerals(CoalBasin.ID)),
                    new BasinGenerator(params), new SeamModel(params),
                    new SurfaceHint(params.surface()), new Accessories(params.accessories()), new Memo<>(CACHE_LIMIT));
            state = current;
        }
        return current;
    }

    /** Drops the cached objects and measured basins so the next use rebuilds them. */
    public static void invalidate() {
        state = null;
    }

    /**
     * Returns the active generation parameters.
     *
     * @return parameters read from the config
     */
    public static CoalParams params() {
        return state().params();
    }

    /**
     * Returns what the basin places for the current ore selection.
     *
     * @return seam blocks, coal ranks and accompanying minerals
     */
    public static CoalOres.Seams seams() {
        return state().seams();
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
     * Returns the shared rule for loose coal and coaly soil.
     *
     * @return rule for the active parameters
     */
    public static SurfaceHint surface() {
        return state().surface();
    }

    /**
     * Returns the shared rules for the minerals that accompany the coal.
     *
     * @return rules for the active parameters
     */
    public static Accessories accessories() {
        return state().accessories();
    }

    /**
     * Lists the basins with coal that may touch an area, measuring the terrain of each one on first use.
     *
     * @param seed world seed
     * @param probe terrain source
     * @param minX west edge of the area in blocks
     * @param minZ north edge of the area in blocks
     * @param maxX east edge of the area in blocks
     * @param maxZ south edge of the area in blocks
     * @return basins outside high mountains
     */
    public static List<ResolvedBasin> basinsNear(long seed, TerrainProbe probe, int minX, int minZ, int maxX,
            int maxZ) {
        State current = state();
        return resolved(current, probe, current.generator().basinsNear(seed, minX, minZ, maxX, maxZ).stream())
                .toList();
    }

    /**
     * Finds the basin of a type nearest to a position, measuring basins from near to far.
     *
     * @param seed world seed
     * @param probe terrain source
     * @param type wanted type
     * @param x world X
     * @param z world Z
     * @param radius search limit in blocks
     * @return the nearest basin of that type, empty if none is among the nearest candidates
     */
    public static Optional<ResolvedBasin> nearest(long seed, TerrainProbe probe, BasinType type, int x, int z,
            int radius) {
        State current = state();
        Stream<Basin> candidates = current.generator()
                .basinsNear(seed, x - radius, z - radius, x + radius, z + radius).stream()
                .filter(basin -> type == BasinType.FOLDED || basin.rolledType() == type)
                .sorted(Comparator.comparingDouble(basin -> Math.hypot(basin.x() - x, basin.z() - z)))
                .limit(MAX_CANDIDATES);
        return resolved(current, probe, candidates).filter(basin -> basin.type() == type).findFirst();
    }

    private static Stream<ResolvedBasin> resolved(State current, TerrainProbe probe, Stream<Basin> basins) {
        return basins.flatMap(basin -> current.basins().get(basin,
                key -> resolve(basin, probe, current.params())).stream());
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
