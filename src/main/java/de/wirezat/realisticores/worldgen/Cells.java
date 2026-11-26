package de.wirezat.realisticores.worldgen;

/** Square grid on which deposits are scattered: at most one deposit per cell, offset from the cell centre. */
public final class Cells {
    private Cells() {
    }

    /** Receives the indices of one grid cell. */
    @FunctionalInterface
    public interface Visitor {
        /**
         * Handles one cell.
         *
         * @param cellX grid cell X index
         * @param cellZ grid cell Z index
         */
        void visit(int cellX, int cellZ);
    }

    /**
     * Draws the centre of a deposit along one axis of its cell.
     *
     * @param rng random stream of the cell
     * @param cell cell index along the axis
     * @param cellSize edge of a cell in blocks
     * @param jitter largest offset from the cell centre as a fraction of the cell size
     * @return world coordinate of the centre in blocks
     */
    public static int center(SplitMix64 rng, int cell, int cellSize, double jitter) {
        return (int) ((cell + 0.5 + rng.nextDouble(-1.0, 1.0) * jitter) * cellSize);
    }

    /**
     * Visits every cell whose deposit may reach a block rectangle.
     *
     * @param minX west edge of the rectangle in blocks
     * @param minZ north edge of the rectangle in blocks
     * @param maxX east edge of the rectangle in blocks
     * @param maxZ south edge of the rectangle in blocks
     * @param reach farthest a deposit reaches from its cell, in blocks
     * @param cellSize edge of a cell in blocks
     * @param visitor receives each cell
     */
    public static void around(int minX, int minZ, int maxX, int maxZ, double reach, int cellSize, Visitor visitor) {
        int lastX = Math.floorDiv((int) (maxX + reach), cellSize);
        int lastZ = Math.floorDiv((int) (maxZ + reach), cellSize);
        for (int cellX = Math.floorDiv((int) (minX - reach), cellSize); cellX <= lastX; cellX++) {
            for (int cellZ = Math.floorDiv((int) (minZ - reach), cellSize); cellZ <= lastZ; cellZ++) {
                visitor.visit(cellX, cellZ);
            }
        }
    }
}
