package de.wirezat.realisticores.porphyry;

/**
 * One porphyry copper deposit.
 *
 * @param x world X of the stock axis
 * @param z world Z of the stock axis
 * @param radius radius of the ore shell in blocks
 * @param elongation stretch along the long axis, at least 1
 * @param angle direction of the long axis in radians
 * @param stage erosion stage
 * @param surfaceY original terrain height at the axis
 * @param salt per-deposit offset for its noise
 */
public record Body(int x, int z, int radius, double elongation, double angle, Stage stage, int surfaceY, long salt) {
}
