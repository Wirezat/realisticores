package de.wirezat.realisticores.kimberlite;

/** Decides which world positions lie on a craton, using low-frequency value noise. */
public final class CratonMask {
    private static final long SALT = 0x4352415F4B494D42L;

    private final KimberliteParams.CratonParams params;

    /**
     * Creates a craton mask.
     *
     * @param params wavelength and threshold of the mask
     */
    public CratonMask(KimberliteParams.CratonParams params) {
        this.params = params;
    }

    /**
     * Checks whether a position lies on a craton.
     *
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @return true if the noise value at the position exceeds the threshold
     */
    public boolean contains(long seed, double x, double z) {
        return noise(seed ^ SALT, x / params.wavelength(), z / params.wavelength()) > params.threshold();
    }

    private static double noise(long seed, double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        double tx = smooth(x - x0);
        double tz = smooth(z - z0);
        double top = lerp(lattice(seed, x0, z0), lattice(seed, x0 + 1, z0), tx);
        double bottom = lerp(lattice(seed, x0, z0 + 1), lattice(seed, x0 + 1, z0 + 1), tx);
        return lerp(top, bottom, tz);
    }

    private static double lattice(long seed, int x, int z) {
        return (SplitMix64.hash(seed, x, z) >>> 11) * 0x1.0p-52 - 1.0;
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
