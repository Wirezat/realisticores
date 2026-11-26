package de.wirezat.realisticores.worldgen;

/**
 * Range of whole numbers from a config, both bounds included.
 *
 * @param min lowest value
 * @param max highest value
 */
public record IntRange(int min, int max) {
    /**
     * Repairs an upper bound below the lower bound.
     *
     * @return this range, or one whose upper bound is raised to the lower bound
     */
    public IntRange ordered() {
        return new IntRange(min, Math.max(min, max));
    }

    /**
     * Draws a value.
     *
     * @param rng random stream
     * @return uniform whole number in the range
     */
    public int pick(SplitMix64 rng) {
        return rng.nextInt(min, max);
    }

    /**
     * Moves a value into the range.
     *
     * @param value any value
     * @return the nearest value inside the range
     */
    public int clamp(int value) {
        return Math.max(min, Math.min(max, value));
    }
}
