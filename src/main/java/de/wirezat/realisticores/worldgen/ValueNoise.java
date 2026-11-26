package de.wirezat.realisticores.worldgen;

/** Seeded value noise with smooth interpolation, output in [-1, 1). */
public final class ValueNoise {
    private ValueNoise() {
    }

    /**
     * Samples 2D noise.
     *
     * @param seed noise seed
     * @param x X in noise units (one lattice cell per unit)
     * @param z Z in noise units
     * @return noise value in [-1, 1)
     */
    public static double sample2(long seed, double x, double z) {
        return sample2Layer(seed, x, z, 0);
    }

    /**
     * Samples 2D noise with features of a given size.
     *
     * @param seed noise seed
     * @param x X in blocks
     * @param z Z in blocks
     * @param wavelength size of one lattice cell in blocks
     * @return noise value in [-1, 1)
     */
    public static double sample2(long seed, double x, double z, double wavelength) {
        return sample2(seed, x / wavelength, z / wavelength);
    }

    /**
     * Samples 3D noise.
     *
     * @param seed noise seed
     * @param x X in noise units
     * @param y Y in noise units
     * @param z Z in noise units
     * @return noise value in [-1, 1)
     */
    public static double sample3(long seed, double x, double y, double z) {
        int y0 = (int) Math.floor(y);
        double ty = smooth(y - y0);
        double low = sample2Layer(seed, x, z, y0);
        double high = sample2Layer(seed, x, z, y0 + 1);
        return lerp(low, high, ty);
    }

    /**
     * Samples 3D noise with features of a given size.
     *
     * @param seed noise seed
     * @param x X in blocks
     * @param y Y in blocks
     * @param z Z in blocks
     * @param wavelength size of one lattice cell in blocks
     * @return noise value in [-1, 1)
     */
    public static double sample3(long seed, double x, double y, double z, double wavelength) {
        return sample3(seed, x / wavelength, y / wavelength, z / wavelength);
    }

    private static double sample2Layer(long seed, double x, double z, int layer) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        double tx = smooth(x - x0);
        double tz = smooth(z - z0);
        double top = lerp(lattice(seed, x0, z0, layer), lattice(seed, x0 + 1, z0, layer), tx);
        double bottom = lerp(lattice(seed, x0, z0 + 1, layer), lattice(seed, x0 + 1, z0 + 1, layer), tx);
        return lerp(top, bottom, tz);
    }

    private static double lattice(long seed, int x, int z, int layer) {
        return (SplitMix64.hash(seed + SplitMix64.mix(layer), x, z) >>> 11) * 0x1.0p-52 - 1.0;
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
