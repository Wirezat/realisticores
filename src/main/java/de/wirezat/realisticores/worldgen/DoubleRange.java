package de.wirezat.realisticores.worldgen;

/**
 * Range of decimal numbers from a config.
 *
 * @param min lower bound
 * @param max upper bound
 */
public record DoubleRange(double min, double max) {
    /**
     * Repairs an upper bound below the lower bound.
     *
     * @return this range, or one whose upper bound is raised to the lower bound
     */
    public DoubleRange ordered() {
        return new DoubleRange(min, Math.max(min, max));
    }

    /**
     * Draws a value.
     *
     * @param rng random stream
     * @return uniform value in [min, max)
     */
    public double pick(SplitMix64 rng) {
        return rng.nextDouble(min, max);
    }

    /**
     * Gives the value at a position in the range.
     *
     * @param t position, 0 at the lower and 1 at the upper bound
     * @return the interpolated value
     */
    public double lerp(double t) {
        return min + (max - min) * t;
    }
}
