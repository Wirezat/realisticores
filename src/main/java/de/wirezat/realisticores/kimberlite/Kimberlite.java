package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.KimberliteConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared generator objects of kimberlite generation, rebuilt whenever the config changes. */
public final class Kimberlite {
    private static final Logger LOGGER = LoggerFactory.getLogger(Kimberlite.class);
    private static final ForkJoinPool TRACERS =
            new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

    private static final int SPOT_SEARCH_MARGIN = 300;

    private static volatile State state;

    private Kimberlite() {
    }

    private record PipeKey(long seed, Pipe pipe) {
    }

    private record State(KimberliteParams params, FieldGenerator generator, PipeProfile profile, FaciesSelector facies,
            Crater crater, PipeGrade grade, PipeShape shape, ConcurrentMap<PipeKey, PlumePath> plumes,
            ConcurrentMap<PipeKey, Double> factors, ConcurrentMap<PipeKey, CraterSite> sites) {
    }

    private static State state() {
        State current = state;
        if (current == null) {
            KimberliteParams params = KimberliteConfig.params();
            PipeProfile profile = new PipeProfile(params.pipe());
            current = new State(params, new FieldGenerator(params), profile, new FaciesSelector(params),
                    new Crater(params.crater()), new PipeGrade(params.grade()), new PipeShape(params.pipe(), profile),
                    new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
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
     * @param seed world seed
     * @param pipe the pipe
     * @param probe terrain and climate source
     * @return the site of the crater
     */
    public static CraterSite craterSite(long seed, Pipe pipe, TerrainProbe probe) {
        State current = state();
        return current.sites().computeIfAbsent(new PipeKey(seed, pipe),
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
                        <= params.maxLength() * plumeFactor(current, seed, pipe, probe) + pipe.diameter())
                .map(pipe -> plume(current, seed, pipe, probe, seaLevel))
                .toList(), TRACERS);
    }

    /**
     * Picks a trail point for a map on the trail pool: from the first pipe whose trail has a suitable one.
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
        return CompletableFuture.supplyAsync(() -> {
            for (Pipe pipe : pipes) {
                PlumePath path = plume(current, seed, pipe, probe, seaLevel);
                List<Plume.Spot> spots = Plume.mapSpots(path, pipesAround(current, seed, path), probe, seaLevel,
                        current.params().plume());
                if (!spots.isEmpty()) {
                    return Optional.of(spots.get(Math.floorMod(pick, spots.size())));
                }
            }
            return Optional.empty();
        }, TRACERS);
    }

    private static List<Pipe> pipesAround(State current, long seed, PlumePath path) {
        int minX = (int) Arrays.stream(path.xs()).min().orElseThrow() - SPOT_SEARCH_MARGIN;
        int minZ = (int) Arrays.stream(path.zs()).min().orElseThrow() - SPOT_SEARCH_MARGIN;
        int maxX = (int) Arrays.stream(path.xs()).max().orElseThrow() + SPOT_SEARCH_MARGIN;
        int maxZ = (int) Arrays.stream(path.zs()).max().orElseThrow() + SPOT_SEARCH_MARGIN;
        return current.generator().pipesNear(seed, minX, minZ, maxX, maxZ);
    }

    /**
     * Logs the failure of a background task; meant for {@link CompletableFuture#whenComplete}.
     *
     * @param result result of the task, ignored
     * @param failure what went wrong, or null if the task succeeded
     */
    public static void logFailure(Object result, Throwable failure) {
        if (failure != null) {
            LOGGER.error("Background survey task failed", failure);
        }
    }

    private static double distanceToArea(Pipe pipe, int minX, int minZ, int maxX, int maxZ) {
        double dx = Math.max(0, Math.max(minX - pipe.x(), pipe.x() - maxX));
        double dz = Math.max(0, Math.max(minZ - pipe.z(), pipe.z() - maxZ));
        return Math.hypot(dx, dz);
    }

    private static PlumePath plume(State current, long seed, Pipe pipe, TerrainProbe probe, int seaLevel) {
        return current.plumes().computeIfAbsent(new PipeKey(seed, pipe), key -> Plume.trace(pipe,
                Plume.fallbackAngle(seed, pipe, current.params().field().cellSize()), probe, seaLevel,
                current.params().plume()));
    }

    private static double plumeFactor(State current, long seed, Pipe pipe, TerrainProbe probe) {
        return current.factors().computeIfAbsent(new PipeKey(seed, pipe),
                key -> Plume.lengthFactor(pipe, probe, current.params().plume()));
    }
}
