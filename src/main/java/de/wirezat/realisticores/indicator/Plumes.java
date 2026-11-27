package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.worldgen.Memo;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

/**
 * The traced indicator trails of one deposit kind, traced on demand on a thread pool shared by all kinds.
 *
 * @param <S> kind of deposit
 */
public final class Plumes<S extends TrailSource> {
    private static final ForkJoinPool TRACERS =
            new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
    private static final int SPOT_SEARCH_MARGIN = 300;
    private static final int CACHE_LIMIT = 4096;
    private static final double[] SPOT_DISTANCES = {250.0, 150.0};

    private final PlumeParams params;
    private final int areaSize;
    private final Finder<S> finder;
    private final Memo<S, PlumePath<S>> paths = new Memo<>(CACHE_LIMIT);
    private final Memo<S, Double> factors = new Memo<>(CACHE_LIMIT);

    /**
     * Lists the deposits of one kind in an area.
     *
     * @param <S> kind of deposit
     */
    @FunctionalInterface
    public interface Finder<S> {
        /**
         * Lists the deposits whose centre may lie in a rectangle.
         *
         * @param seed world seed
         * @param probe terrain and climate source
         * @param minX west edge in blocks
         * @param minZ north edge in blocks
         * @param maxX east edge in blocks
         * @param maxZ south edge in blocks
         * @return the deposits
         */
        List<S> near(long seed, TerrainProbe probe, int minX, int minZ, int maxX, int maxZ);
    }

    /**
     * Creates an empty trail store.
     *
     * @param params trail settings
     * @param areaSize edge of the area in blocks whose deposits share one flow direction on flat terrain
     * @param finder lists the deposits of an area
     */
    public Plumes(PlumeParams params, int areaSize, Finder<S> finder) {
        this.params = params;
        this.areaSize = areaSize;
        this.finder = finder;
    }

    /**
     * Collects the trails that can reach an area, tracing missing ones on the trail pool.
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
    public CompletableFuture<List<PlumePath<S>>> reaching(long seed, TerrainProbe probe, int seaLevel, int minX,
            int minZ, int maxX, int maxZ) {
        return CompletableFuture.supplyAsync(() -> collect(seed, probe, seaLevel, minX, minZ, maxX, maxZ), TRACERS);
    }

    /**
     * Picks a trail point for a map on the trail pool: where the strongest trail comes from a wanted deposit and no
     * deposit is near, else closer to a deposit, else on any trail.
     *
     * @param seed world seed
     * @param probe terrain and climate source, read from the pool's threads
     * @param seaLevel Y of the sea surface
     * @param sources deposits to try, in this order
     * @param pick random number that selects one of the suitable points
     * @param wanted tells which deposits the map should lead to
     * @return future of the point, empty if no trail has one
     */
    public CompletableFuture<Optional<Plume.Spot>> mapSpot(long seed, TerrainProbe probe, int seaLevel,
            List<S> sources, int pick, Predicate<? super S> wanted) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlumePath<S>> own = sources.parallelStream()
                    .map(source -> path(seed, source, probe, seaLevel)).toList();
            Optional<Plume.Spot> any = Optional.empty();
            for (double distance : SPOT_DISTANCES) {
                for (PlumePath<S> path : own) {
                    List<Plume.Spot> spots = Plume.mapSpots(path, sourcesAround(seed, probe, path), distance, probe,
                            seaLevel, params);
                    List<Plume.Spot> good = spots.isEmpty() ? spots : Plume.keepFrom(spots,
                            collect(seed, probe, seaLevel, minOf(path.xs()), minOf(path.zs()), maxOf(path.xs()),
                                    maxOf(path.zs())), params, wanted);
                    if (!good.isEmpty()) {
                        return Optional.of(good.get(Math.floorMod(pick, good.size())));
                    }
                    if (any.isEmpty() && !spots.isEmpty()) {
                        any = Optional.of(spots.get(Math.floorMod(pick, spots.size())));
                    }
                }
            }
            return any;
        }, TRACERS);
    }

    /**
     * Collects the trails that can reach an area on the calling thread.
     *
     * @param seed world seed
     * @param probe terrain and climate source
     * @param seaLevel Y of the sea surface
     * @param minX west edge of the area in blocks
     * @param minZ north edge of the area in blocks
     * @param maxX east edge of the area in blocks
     * @param maxZ south edge of the area in blocks
     * @return the trails of all deposits close enough to the area
     */
    private List<PlumePath<S>> collect(long seed, TerrainProbe probe, int seaLevel, int minX, int minZ, int maxX,
            int maxZ) {
        int reach = (int) Math.ceil(params.maxLength() * Plume.maxFactor(params));
        return finder.near(seed, probe, minX - reach, minZ - reach, maxX + reach, maxZ + reach).parallelStream()
                .filter(source -> distanceToArea(source, minX, minZ, maxX, maxZ)
                        <= params.maxLength() * factor(source, probe) + 2.0 * source.outcrop())
                .map(source -> path(seed, source, probe, seaLevel))
                .toList();
    }

    /**
     * Lists the deposits near a trail.
     *
     * @param seed world seed
     * @param probe terrain and climate source
     * @param path the trail
     * @return deposits within the search margin of the trail's bounding box
     */
    private List<S> sourcesAround(long seed, TerrainProbe probe, PlumePath<S> path) {
        return finder.near(seed, probe, minOf(path.xs()) - SPOT_SEARCH_MARGIN, minOf(path.zs()) - SPOT_SEARCH_MARGIN,
                maxOf(path.xs()) + SPOT_SEARCH_MARGIN, maxOf(path.zs()) + SPOT_SEARCH_MARGIN);
    }

    /**
     * Gives the trail of a deposit, tracing it on first use.
     *
     * @param seed world seed
     * @param source the deposit
     * @param probe terrain and climate source
     * @param seaLevel Y of the sea surface
     * @return the trail
     */
    private PlumePath<S> path(long seed, S source, TerrainProbe probe, int seaLevel) {
        return paths.get(source, key -> Plume.trace(source, Plume.fallbackAngle(seed, source, areaSize), probe,
                seaLevel, params));
    }

    /**
     * Gives the climate length factor of a deposit, measuring it on first use.
     *
     * @param source the deposit
     * @param probe terrain and climate source
     * @return the factor
     */
    private double factor(S source, TerrainProbe probe) {
        return factors.get(source, key -> Plume.lengthFactor(source, probe, params));
    }

    /**
     * Measures the distance from a deposit centre to a rectangle.
     *
     * @param source the deposit
     * @param minX west edge in blocks
     * @param minZ north edge in blocks
     * @param maxX east edge in blocks
     * @param maxZ south edge in blocks
     * @return distance in blocks, 0 inside
     */
    private static double distanceToArea(TrailSource source, int minX, int minZ, int maxX, int maxZ) {
        double dx = Math.max(0, Math.max(minX - source.x(), source.x() - maxX));
        double dz = Math.max(0, Math.max(minZ - source.z(), source.z() - maxZ));
        return Math.hypot(dx, dz);
    }

    /**
     * Gives the smallest coordinate of a path.
     *
     * @param values coordinates
     * @return the smallest, rounded toward zero
     */
    private static int minOf(double[] values) {
        return (int) Arrays.stream(values).min().orElseThrow();
    }

    /**
     * Gives the largest coordinate of a path.
     *
     * @param values coordinates
     * @return the largest, rounded toward zero
     */
    private static int maxOf(double[] values) {
        return (int) Arrays.stream(values).max().orElseThrow();
    }
}
