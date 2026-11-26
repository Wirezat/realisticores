package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.TerrainProbe;
import de.wirezat.realisticores.worldgen.Memo;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/** Shared generator objects of kimberlite generation, rebuilt whenever the config changes. */
public final class Kimberlite {
    private static final ForkJoinPool TRACERS =
            new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

    private static final int SPOT_SEARCH_MARGIN = 300;
    private static final int CACHE_LIMIT = 4096;
    private static final double[] SPOT_DISTANCES = {250.0, 150.0};

    private static volatile State state;

    private Kimberlite() {
    }

    private record State(KimberliteParams params, FieldGenerator generator, PipeProfile profile, FaciesSelector facies,
            Crater crater, PipeGrade grade, PipeShape shape, Memo<Pipe, PlumePath> plumes,
            Memo<Pipe, Double> factors, Memo<Pipe, CraterSite> sites) {
    }

    private static State state() {
        State current = state;
        if (current == null) {
            KimberliteParams params = KimberliteConfig.params();
            PipeProfile profile = new PipeProfile(params.pipe());
            current = new State(params, new FieldGenerator(params), profile, new FaciesSelector(params),
                    new Crater(params.crater()), new PipeGrade(params.grade()), new PipeShape(params.pipe(), profile),
                    new Memo<>(CACHE_LIMIT), new Memo<>(CACHE_LIMIT), new Memo<>(CACHE_LIMIT));
            state = current;
        }
        return current;
    }

    /** Drops the cached objects and traced trails so the next use rebuilds them. */
    public static void invalidate() {
        state = null;
    }

    /**
     * Returns the active parameters.
     *
     * @return kimberlite generation parameters
     */
    public static KimberliteParams params() {
        return state().params();
    }

    /**
     * Returns the shared field generator.
     *
     * @return generator for the active parameters
     */
    public static FieldGenerator generator() {
        return state().generator();
    }

    /**
     * Returns the shared pipe profile.
     *
     * @return profile for the active parameters
     */
    public static PipeProfile profile() {
        return state().profile();
    }

    /**
     * Returns the shared rock type selector.
     *
     * @return selector for the active parameters
     */
    public static FaciesSelector facies() {
        return state().facies();
    }

    /**
     * Returns the shared crater model.
     *
     * @return crater model for the active parameters
     */
    public static Crater crater() {
        return state().crater();
    }

    /**
     * Returns the shared ore grade model.
     *
     * @return grade model for the active parameters
     */
    public static PipeGrade grade() {
        return state().grade();
    }

    /**
     * Returns the shared pipe shape model.
     *
     * @return shape model for the active parameters
     */
    public static PipeShape shape() {
        return state().shape();
    }

    /**
     * Gives the terrain facts around a pipe's crater, measuring them on first use.
     *
     * @param pipe the pipe
     * @param probe terrain and climate source
     * @return the site of the crater
     */
    public static CraterSite craterSite(Pipe pipe, TerrainProbe probe) {
        State current = state();
        return current.sites().get(pipe,
                key -> CraterSite.measure(pipe, probe, current.params().crater().ringPoints()));
    }

    /**
     * Collects the indicator trails that can reach an area, tracing missing ones on a thread pool of their own.
     *
     * @param seed world seed
     * @param probe terrain and climate source, read from the pool's threads
     * @param seaLevel Y of the sea surface
     * @param minX west edge of the area in blocks
     * @param minZ north edge of the area in blocks
     * @param maxX east edge of the area in blocks
     * @param maxZ south edge of the area in blocks
     * @return future of the trails of all pipes close enough to the area
     */
    public static CompletableFuture<List<PlumePath>> plumesReaching(long seed, TerrainProbe probe, int seaLevel,
            int minX, int minZ, int maxX, int maxZ) {
        State current = state();
        KimberliteParams.PlumeParams params = current.params().plume();
        int reach = (int) Math.ceil(params.maxLength() * Plume.maxFactor(params));
        return CompletableFuture.supplyAsync(() -> current.generator()
                .pipesNear(seed, minX - reach, minZ - reach, maxX + reach, maxZ + reach).parallelStream()
                .filter(pipe -> distanceToArea(pipe, minX, minZ, maxX, maxZ)
                        <= params.maxLength() * plumeFactor(current, pipe, probe) + pipe.diameter())
                .map(pipe -> plume(current, seed, pipe, probe, seaLevel))
                .toList(), TRACERS);
    }

    /**
     * Picks a trail point for a map on the trail pool: where the strongest trail has diamonds and no pipe is near,
     * else closer to a pipe, else on any trail.
     *
     * @param seed world seed
     * @param probe terrain and climate source, read from the pool's threads
     * @param seaLevel Y of the sea surface
     * @param pipes pipes to try, in this order
     * @param pick random number that selects one of the suitable points
     * @return future of the point, empty if no trail has one
     */
    public static CompletableFuture<Optional<Plume.Spot>> mapSpot(long seed, TerrainProbe probe, int seaLevel,
            List<Pipe> pipes, int pick) {
        State current = state();
        KimberliteParams.PlumeParams params = current.params().plume();
        return CompletableFuture.supplyAsync(() -> {
            List<PlumePath> own = pipes.parallelStream()
                    .map(pipe -> plume(current, seed, pipe, probe, seaLevel)).toList();
            Optional<Plume.Spot> any = Optional.empty();
            for (double distance : SPOT_DISTANCES) {
                for (PlumePath path : own) {
                    List<Plume.Spot> spots = Plume.mapSpots(path, pipesAround(current, seed, path), distance, probe,
                            seaLevel, params);
                    List<Plume.Spot> good = spots.isEmpty() ? spots : Plume.withDiamonds(spots,
                            plumesReaching(seed, probe, seaLevel, minOf(path.xs()), minOf(path.zs()),
                                    maxOf(path.xs()), maxOf(path.zs())).join(), current.grade(), params);
                    if (!good.isEmpty()) {
                        return Optional.of(choose(good, pick));
                    }
                    if (any.isEmpty() && !spots.isEmpty()) {
                        any = Optional.of(choose(spots, pick));
                    }
                }
            }
            return any;
        }, TRACERS);
    }

    private static Plume.Spot choose(List<Plume.Spot> spots, int pick) {
        return spots.get(Math.floorMod(pick, spots.size()));
    }

    private static int minOf(double[] values) {
        return (int) Arrays.stream(values).min().orElseThrow();
    }

    private static int maxOf(double[] values) {
        return (int) Arrays.stream(values).max().orElseThrow();
    }

    private static List<Pipe> pipesAround(State current, long seed, PlumePath path) {
        int minX = minOf(path.xs()) - SPOT_SEARCH_MARGIN;
        int minZ = minOf(path.zs()) - SPOT_SEARCH_MARGIN;
        int maxX = maxOf(path.xs()) + SPOT_SEARCH_MARGIN;
        int maxZ = maxOf(path.zs()) + SPOT_SEARCH_MARGIN;
        return current.generator().pipesNear(seed, minX, minZ, maxX, maxZ);
    }

    private static double distanceToArea(Pipe pipe, int minX, int minZ, int maxX, int maxZ) {
        double dx = Math.max(0, Math.max(minX - pipe.x(), pipe.x() - maxX));
        double dz = Math.max(0, Math.max(minZ - pipe.z(), pipe.z() - maxZ));
        return Math.hypot(dx, dz);
    }

    private static PlumePath plume(State current, long seed, Pipe pipe, TerrainProbe probe, int seaLevel) {
        return current.plumes().get(pipe, key -> Plume.trace(pipe,
                Plume.fallbackAngle(seed, pipe, current.params().field().cellSize()), probe, seaLevel,
                current.params().plume()));
    }

    private static double plumeFactor(State current, Pipe pipe, TerrainProbe probe) {
        return current.factors().get(pipe,
                key -> Plume.lengthFactor(pipe, probe, current.params().plume()));
    }
}
