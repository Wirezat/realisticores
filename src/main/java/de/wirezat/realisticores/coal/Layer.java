package de.wirezat.realisticores.coal;

/**
 * One rock layer of a column.
 *
 * @param minY lowest Y of the layer
 * @param maxY highest Y of the layer
 * @param rock rock of the layer
 */
public record Layer(int minY, int maxY, Rock rock) {
}
