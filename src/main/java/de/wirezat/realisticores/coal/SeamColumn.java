package de.wirezat.realisticores.coal;

import java.util.List;

/**
 * All layers of a basin in one column.
 *
 * @param rank rank of the coal in the column
 * @param layers layers from bottom to top without overlap
 * @param edge distance to the basin centre in basin units, 0 in the centre and 1 at the rim
 * @param basinSeed seed of the basin the column belongs to
 * @param radius mean radius of that basin in blocks
 */
public record SeamColumn(Rank rank, List<Layer> layers, double edge, long basinSeed, double radius) {
}
