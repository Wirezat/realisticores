package de.wirezat.realisticores.kimberlite;

/** Irregular outline of a pipe: elliptical base shape plus noisy walls. */
public final class PipeShape {
    private static final long SHAPE_SALT = 0x5348415045504950L;
    private static final long OUTCROP_SALT = 0x4F5554435250504CL;

    private final KimberliteParams.PipeParams params;
    private final PipeProfile profile;

    /**
     * Creates a shape model.
     *
     * @param params pipe settings
     * @param profile radius profile by height
     */
    public PipeShape(KimberliteParams.PipeParams params, PipeProfile profile) {
        this.params = params;
        this.profile = profile;
    }

    /**
     * Gives the largest horizontal distance from the pipe centre at which the pipe can exist.
     *
     * @param pipe the pipe
     * @return reach in blocks
     */
    public double reach(Pipe pipe) {
        return profile.maxRadius(pipe) * (1.0 + params.wallNoiseStrength());
    }

    /**
     * Checks whether a block position lies inside the pipe.
     *
     * @param pipe the pipe
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @return true if the position is inside the irregular outline
     */
    public boolean contains(Pipe pipe, int x, int y, int z) {
        long shapeSeed = SplitMix64.hash(SHAPE_SALT, pipe.x(), pipe.z());
        SplitMix64 rng = new SplitMix64(shapeSeed);
        double angle = rng.nextDouble() * Math.PI;
        double aspect = params.minAspect() + (1.0 - params.minAspect()) * rng.nextDouble();
        double dx = x - pipe.x();
        double dz = z - pipe.z();
        double along = dx * Math.cos(angle) + dz * Math.sin(angle);
        double across = (-dx * Math.sin(angle) + dz * Math.cos(angle)) / aspect;
        double distance = Math.hypot(along, across);
        double wavelength = Math.max(10.0, pipe.diameter() * 0.3);
        double noise = 0.65 * ValueNoise.sample3(shapeSeed, x / wavelength, y / (wavelength * 2.0), z / wavelength)
                + 0.35 * ValueNoise.sample3(shapeSeed + 1, x / (wavelength * 0.4), y / (wavelength * 0.8),
                        z / (wavelength * 0.4));
        double radius = profile.radiusAt(pipe, y) * (1.0 + params.wallNoiseStrength() * noise);
        return distance <= radius;
    }

    /**
     * Checks whether kimberlite shows at the surface in a column instead of staying under the soil.
     *
     * @param x world X of the column
     * @param z world Z of the column
     * @return true if the column is an outcrop
     */
    public boolean isOutcrop(int x, int z) {
        double wavelength = params.outcropWavelength();
        return ValueNoise.sample2(OUTCROP_SALT, x / wavelength, z / wavelength) > params.outcropThreshold();
    }
}
