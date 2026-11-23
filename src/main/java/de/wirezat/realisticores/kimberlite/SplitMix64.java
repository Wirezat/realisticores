package de.wirezat.realisticores.kimberlite;

/** Deterministic 64-bit hash and random stream (SplitMix64). */
public final class SplitMix64 {
    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private long state;

    /**
     * Creates a random stream.
     *
     * @param seed initial state; equal seeds give equal streams
     */
    public SplitMix64(long seed) {
        this.state = seed;
    }

    /**
     * Mixes a value into a well-distributed 64-bit hash.
     *
     * @param value input value
     * @return mixed hash
     */
    public static long mix(long value) {
        long z = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Hashes a seed with two integer coordinates.
     *
     * @param seed world seed or salted seed
     * @param a first coordinate
     * @param b second coordinate
     * @return hash that is stable for equal inputs
     */
    public static long hash(long seed, int a, int b) {
        return mix(seed + GOLDEN * mix(a + GOLDEN * mix(b)));
    }

    /**
     * Advances the stream.
     *
     * @return next 64 random bits
     */
    public long nextLong() {
        state += GOLDEN;
        return mix(state);
    }

    /**
     * Advances the stream.
     *
     * @return uniform double in [0, 1)
     */
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /**
     * Advances the stream by two draws.
     *
     * @return standard normal distributed double
     */
    public double nextGaussian() {
        double u1 = 1.0 - nextDouble();
        double u2 = nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }
}
