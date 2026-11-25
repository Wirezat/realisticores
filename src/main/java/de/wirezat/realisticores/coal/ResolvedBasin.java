package de.wirezat.realisticores.coal;

/**
 * A basin fitted to the terrain.
 *
 * @param basin the basin
 * @param type type after the terrain was measured
 * @param datum terrain height all seams refer to
 * @param layout seams, faults and folds
 */
public record ResolvedBasin(Basin basin, BasinType type, int datum, BasinLayout layout) {
}
