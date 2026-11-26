package de.wirezat.realisticores.worldgen;

import net.minecraft.world.level.ChunkPos;

/** Walks the 16 x 16 block columns of a chunk. */
public final class ChunkColumns {
    /** Work done on one column. */
    @FunctionalInterface
    public interface Visitor {
        /**
         * Handles one column.
         *
         * @param localX X within the chunk, 0 to 15
         * @param localZ Z within the chunk, 0 to 15
         * @param x world X
         * @param z world Z
         * @return true if the column was changed
         */
        boolean visit(int localX, int localZ, int x, int z);
    }

    private ChunkColumns() {
    }

    /**
     * Visits every column of a chunk.
     *
     * @param chunk the chunk
     * @param visitor work done on each column
     * @return true if any column was changed
     */
    public static boolean forEach(ChunkPos chunk, Visitor visitor) {
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        boolean changed = false;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                changed |= visitor.visit(localX, localZ, minX + localX, minZ + localZ);
            }
        }
        return changed;
    }
}
