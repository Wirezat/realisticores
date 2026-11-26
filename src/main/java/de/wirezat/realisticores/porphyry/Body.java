package de.wirezat.realisticores.porphyry;

import java.util.List;

/**
 * One porphyry copper deposit.
 *
 * @param x world X of the stock axis at the dome centre
 * @param z world Z of the stock axis at the dome centre
 * @param radius radius of the ore shell in blocks
 * @param elongation stretch along the long axis, at least 1
 * @param angle direction of the long axis in radians
 * @param stage erosion stage
 * @param surfaceY original terrain height at the axis
 * @param salt per-deposit offset for its noise
 * @param tiltX horizontal shift of the axis in X per block of height
 * @param tiltZ horizontal shift of the axis in Z per block of height
 * @param vents breccia vents
 * @param dikes porphyry dikes
 */
public record Body(int x, int z, int radius, double elongation, double angle, Stage stage, int surfaceY, long salt,
        double tiltX, double tiltZ, List<Vent> vents, List<Dike> dikes) {
    /**
     * A vertical breccia vent.
     *
     * @param u position along the long axis, in units of the radius before elongation
     * @param v position across the long axis, in units of the radius
     * @param radius vent radius in blocks
     */
    public record Vent(double u, double v, int radius) {
    }

    /**
     * A steep porphyry dike running through the stock.
     *
     * @param angle strike direction in radians
     * @param offset distance of its plane from the axis, in units of the radius
     * @param width thickness in blocks
     */
    public record Dike(double angle, double offset, int width) {
    }
}
