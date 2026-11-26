package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.worldgen.IntRange;

/**
 * Where porphyry copper deposits form: groups of deposits in grid cells, like a stretch of a porphyry belt.
 *
 * @param cellSize grid cell size in blocks; each cell holds at most one group
 * @param chance probability that a cell holds a group
 * @param jitter largest offset of the group from its cell centre, as a fraction of the cell size
 * @param deposits fewest and most deposits tried per group
 * @param spacing smallest and largest distance between neighbouring deposits of a group in blocks
 * @param radius smallest and largest ore shell radius in blocks
 * @param minHeight lowest terrain height at a deposit's axis
 * @param placementAttempts tries to find a high enough spot for each deposit before it is skipped
 */
public record PorphyryParams(int cellSize, double chance, double jitter, IntRange deposits, IntRange spacing,
        IntRange radius, int minHeight, int placementAttempts) {
    /**
     * Gives the shipped defaults.
     *
     * @return default parameters
     */
    public static PorphyryParams defaults() {
        return new PorphyryParams(4000, 0.5, 0.3, new IntRange(1, 3), new IntRange(300, 800), new IntRange(40, 80),
                90, 8);
    }
}
