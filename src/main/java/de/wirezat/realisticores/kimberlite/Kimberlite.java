package de.wirezat.realisticores.kimberlite;

/** Shared generator instance and parameters of kimberlite generation. */
public final class Kimberlite {
    private static final KimberliteParams PARAMS = KimberliteParams.defaults();
    private static final FieldGenerator GENERATOR = new FieldGenerator(PARAMS);
    private static final PipeProfile PROFILE = new PipeProfile(PARAMS.pipe());
    private static final PipeShape SHAPE = new PipeShape(PARAMS.pipe(), PROFILE);

    private Kimberlite() {
    }

    /**
     * Returns the active parameters.
     *
     * @return kimberlite generation parameters
     */
    public static KimberliteParams params() {
        return PARAMS;
    }

    /**
     * Returns the shared field generator.
     *
     * @return generator for the active parameters
     */
    public static FieldGenerator generator() {
        return GENERATOR;
    }

    /**
     * Returns the shared pipe profile.
     *
     * @return profile for the active parameters
     */
    public static PipeProfile profile() {
        return PROFILE;
    }

    /**
     * Returns the shared pipe shape model.
     *
     * @return shape model for the active parameters
     */
    public static PipeShape shape() {
        return SHAPE;
    }
}
