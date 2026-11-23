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
        return ValueNoise.sample2(seed ^ SALT, x / params.wavelength(), z / params.wavelength())
                > params.threshold();
    }
}
