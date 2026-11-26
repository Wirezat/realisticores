package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;


/**
 * One coal basin with an elliptical outline.
 *
 * @param x centre X in blocks
 * @param z centre Z in blocks
 * @param longRadius half length of the long axis in blocks
 * @param shortRadius half length of the short axis in blocks
 * @param strike direction of the long axis in radians
 * @param rolledType type unless the terrain makes it a folded basin
 * @param seed seed of everything inside the basin
 */
public record Basin(int x, int z, double longRadius, double shortRadius, double strike, BasinType rolledType,
        long seed) {
    private static final int OUTLINE_SALT = 1;

    /**
     * Gives the distance across the long axis.
     *
     * @param px world X
     * @param pz world Z
     * @return signed distance in blocks
     */
    public double across(double px, double pz) {
        return -(px - x) * Math.sin(strike) + (pz - z) * Math.cos(strike);
    }

    /**
     * Gives the distance from the centre in units of the ellipse.
     *
     * @param px world X
     * @param pz world Z
     * @return 0 at the centre, 1 on the ellipse
     */
    public double distance(double px, double pz) {
        double along = (px - x) * Math.cos(strike) + (pz - z) * Math.sin(strike);
        return Math.hypot(along / longRadius, across(px, pz) / shortRadius);
    }

    /**
     * Gives the distance from the centre relative to the irregular outline.
     *
     * @param px world X
     * @param pz world Z
     * @param params outline settings
     * @return 0 at the centre, below 1 inside the basin
     */
    public double edgeDistance(double px, double pz, CoalParams.BasinParams params) {
        double noise = ValueNoise.sample2(SplitMix64.hash(seed, OUTLINE_SALT, 0), px, pz,
                params.outlineWavelength());
        return distance(px, pz) * (1.0 + params.outlineNoise() * noise);
    }
}
