package de.wirezat.realisticores.coal;

import java.util.List;

/**
 * All layers of a basin in one column.
 *
 * @param rank rank of the coal in the column
 * @param layers layers from bottom to top without overlap
 */
public record SeamColumn(Rank rank, List<Layer> layers) {
}
