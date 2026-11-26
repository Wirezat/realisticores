package de.wirezat.realisticores.indicator;

/**
 * One point of the guide line toward the source of indicator grains.
 *
 * @param x world X in blocks
 * @param z world Z in blocks
 * @param dirX X part of the unit direction toward the source
 * @param dirZ Z part of the unit direction toward the source
 */
public record GuidePoint(double x, double z, double dirX, double dirZ) {
}
