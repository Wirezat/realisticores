package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;

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
        return normalizedDistance(pipe, x, y, z) <= 1.0;
    }

    /**
     * Gives the distance from the pipe axis relative to the local wall.
     *
     * @param pipe the pipe
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @return 0 at the axis, 1 at the wall, above 1 outside the pipe
     */
    public double normalizedDistance(Pipe pipe, int x, int y, int z) {
        long shapeSeed = SplitMix64.hash(SHAPE_SALT, pipe.x(), pipe.z());
        SplitMix64 rng = new SplitMix64(shapeSeed);
        double angle = rng.nextDouble() * Math.PI;
        double aspect = params.minAspect() + (1.0 - params.minAspect()) * rng.nextDouble();
        double dx = x - pipe.x();
        double dz = z - pipe.z();
        double along = dx * Math.cos(angle) + dz * Math.sin(angle);
        double across = (-dx * Math.sin(angle) + dz * Math.cos(angle)) / aspect;
        double distance = Math.hypot(along, across);
        double wavelength = Math.max(params.wallMinWavelength(), pipe.diameter() * params.wallWavelengthFactor());
        double stretch = params.wallVerticalStretch();
        double fine = wavelength * params.wallFineScale();
        double noise = (1.0 - params.wallFineShare())
                * ValueNoise.sample3(shapeSeed, x / wavelength, y / (wavelength * stretch), z / wavelength)
                + params.wallFineShare()
                * ValueNoise.sample3(shapeSeed + 1, x / fine, y / (fine * stretch), z / fine);
        double radius = profile.radiusAt(pipe, y) * (1.0 + params.wallNoiseStrength() * noise);
        return radius <= 0.0 ? Double.MAX_VALUE : distance / radius;
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
