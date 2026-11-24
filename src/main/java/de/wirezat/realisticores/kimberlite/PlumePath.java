package de.wirezat.realisticores.kimberlite;

/**
 * Downhill path along which the indicator grains of one pipe are spread.
 *
 * @param pipe the source pipe
 * @param xs X of the path points in blocks
 * @param zs Z of the path points in blocks
 * @param lengths distance in blocks from the pipe along the path to each point
 * @param factor length factor of the pipe's climate that stretches or shortens the grain spread
 */
public record PlumePath(Pipe pipe, double[] xs, double[] zs, double[] lengths, double factor) {
}
