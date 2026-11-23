package de.wirezat.realisticores.kimberlite;

/** Picks the rock type at a position inside a pipe. */
public final class FaciesSelector {
    private static final long SALT = 0x464143494553L;
    private static final double CONTACT_WAVELENGTH = 6.0;
    private static final double XENOLITH_WAVELENGTH = 5.0;
    private static final double PERIDOTITE_WAVELENGTH = 3.0;
    private static final double SERPENTINE_WAVELENGTH = 10.0;
    private static final double WEATHERING_WAVELENGTH = 20.0;
    private static final double BAND_BONUS = 0.4;
    private static final double WALL_BONUS = 0.3;
    private static final double WALL_START = 0.8;

    private final KimberliteParams params;

    /**
     * Creates a selector.
     *
     * @param params generation settings
     */
    public FaciesSelector(KimberliteParams params) {
        this.params = params;
    }

    /**
     * Chooses the rock type of one block inside a pipe.
     *
     * @param pipe the pipe
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @param surfaceY terrain height of the column before carving
     * @param wallDistance distance from the axis relative to the wall (0 axis, 1 wall)
     * @return the rock type; {@link Facies#HOST} means the original rock stays
     */
    public Facies at(Pipe pipe, int x, int y, int z, int surfaceY, double wallDistance) {
        KimberliteParams.FaciesParams f = params.facies();
        long seed = SplitMix64.hash(SALT, pipe.x(), pipe.z());
        double depth = surfaceY - y;
        double weatheringDepth = f.weatheringDepth()
                + f.weatheringNoise() * ValueNoise.sample2(seed, x / WEATHERING_WAVELENGTH, z / WEATHERING_WAVELENGTH);
        if (depth < weatheringDepth) {
            return Facies.WEATHERED;
        }
        if (wallDistance > 1.0 - f.contactFraction()
                && ValueNoise.sample3(seed + 1, x / CONTACT_WAVELENGTH, y / CONTACT_WAVELENGTH,
                        z / CONTACT_WAVELENGTH) > 0.0) {
            return Facies.HOST;
        }
        if (ValueNoise.sample3(seed + 2, x / PERIDOTITE_WAVELENGTH, y / PERIDOTITE_WAVELENGTH,
                z / PERIDOTITE_WAVELENGTH) > f.peridotiteThreshold()) {
            return Facies.PERIDOTITE;
        }
        if (ValueNoise.sample3(seed + 3, x / XENOLITH_WAVELENGTH, y / XENOLITH_WAVELENGTH,
                z / XENOLITH_WAVELENGTH) > f.xenolithThreshold()) {
            return Facies.HOST;
        }
        double bonus = depth < weatheringDepth + f.serpentinizationBand() ? BAND_BONUS : 0.0;
        if (wallDistance > WALL_START) {
            bonus += WALL_BONUS;
        }
        double serpentine = ValueNoise.sample3(seed + 4, x / SERPENTINE_WAVELENGTH, y / SERPENTINE_WAVELENGTH,
                z / SERPENTINE_WAVELENGTH);
        if (serpentine + bonus > f.serpentinizationThreshold()) {
            return Facies.SERPENTINIZED;
        }
        KimberliteParams.PipeParams p = params.pipe();
        if (y > p.craterBaseY()) {
            return Facies.VOLCANICLASTIC;
        }
        return y > p.rootY() ? Facies.BRECCIA : Facies.KIMBERLITE;
    }
}
