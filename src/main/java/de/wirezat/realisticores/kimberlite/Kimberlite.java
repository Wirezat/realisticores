package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.KimberliteConfig;

/** Shared generator objects of kimberlite generation, rebuilt whenever the config changes. */
public final class Kimberlite {
    private static volatile State state;

    private Kimberlite() {
    }

    private record State(KimberliteParams params, FieldGenerator generator, PipeProfile profile, FaciesSelector facies,
            Crater crater, PipeGrade grade, PipeShape shape) {
    }

    private static State state() {
        State current = state;
        if (current == null) {
            KimberliteParams params = KimberliteConfig.params();
            PipeProfile profile = new PipeProfile(params.pipe());
            current = new State(params, new FieldGenerator(params), profile, new FaciesSelector(params),
                    new Crater(params.crater()), new PipeGrade(params.grade()), new PipeShape(params.pipe(), profile));
            state = current;
        }
        return current;
    }

    /** Drops the cached objects so the next use rebuilds them from the config. */
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
}
