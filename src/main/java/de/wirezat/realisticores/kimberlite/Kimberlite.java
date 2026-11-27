package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.indicator.Plume;
import de.wirezat.realisticores.indicator.PlumePath;
import de.wirezat.realisticores.indicator.Plumes;
import de.wirezat.realisticores.worldgen.Memo;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Shared generator objects of kimberlite generation, rebuilt whenever the config changes. */
public final class Kimberlite {
    private static final int CACHE_LIMIT = 4096;

    private static volatile State state;

    private Kimberlite() {
    }

    private record State(KimberliteParams params, FieldGenerator generator, PipeProfile profile, FaciesSelector facies,
            Crater crater, PipeGrade grade, PipeShape shape, Plumes<Pipe> plumes, Memo<Pipe, CraterSite> sites) {
    }

    /**
     * Gives the shared generator objects, building them from the config on first use.
     *
     * @return the current state
     */
    private static State state() {
        State current = state;
        if (current == null) {
            KimberliteParams params = KimberliteConfig.params();
            PipeProfile profile = new PipeProfile(params.pipe());
            FieldGenerator generator = new FieldGenerator(params);
            current = new State(params, generator, profile, new FaciesSelector(params),
                    new Crater(params.crater()), new PipeGrade(params.grade()), new PipeShape(params.pipe(), profile),
                    new Plumes<>(params.plume(), params.field().cellSize(),
                            (seed, probe, minX, minZ, maxX, maxZ) -> generator.pipesNear(seed, minX, minZ, maxX, maxZ)),
                    new Memo<>(CACHE_LIMIT));
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
     * Collects the indicator trails that can reach an area, tracing missing ones on the trail pool.
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
    public static CompletableFuture<List<PlumePath<Pipe>>> plumesReaching(long seed, TerrainProbe probe, int seaLevel,
            int minX, int minZ, int maxX, int maxZ) {
        return state().plumes().reaching(seed, probe, seaLevel, minX, minZ, maxX, maxZ);
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
        return current.plumes().mapSpot(seed, probe, seaLevel, pipes, pick,
                pipe -> current.grade().pipeFactor(pipe) > 0.0);
    }
}
