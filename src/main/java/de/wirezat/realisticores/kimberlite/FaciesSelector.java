package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;

/** Picks the rock type at a position inside a pipe. */
public final class FaciesSelector {
    private static final long SALT = 0x464143494553L;

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
                + f.weatheringNoise() * ValueNoise.sample2(seed, x, z, f.weatheringWavelength());
        if (depth < weatheringDepth) {
            return Facies.WEATHERED;
        }
        if (wallDistance > 1.0 - f.contactFraction()
                && ValueNoise.sample3(seed + 1, x, y, z, f.contactWavelength()) > f.contactThreshold()) {
            return Facies.HOST;
        }
        if (ValueNoise.sample3(seed + 2, x, y, z, f.peridotiteWavelength()) > f.peridotiteThreshold()) {
            return Facies.PERIDOTITE;
        }
        if (ValueNoise.sample3(seed + 3, x, y, z, f.xenolithWavelength()) > f.xenolithThreshold()) {
            return Facies.HOST;
        }
        double bonus = depth < weatheringDepth + f.serpentinizationBand() ? f.serpentineBandBonus() : 0.0;
        if (wallDistance > f.serpentineWallStart()) {
            bonus += f.serpentineWallBonus();
        }
        double serpentine = ValueNoise.sample3(seed + 4, x, y, z, f.serpentineWavelength());
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
