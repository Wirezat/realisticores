package de.wirezat.realisticores.ore;

import de.wirezat.realisticores.worldgen.SplitMix64;


/** Deterministic random roll deciding whether a block becomes a given ore. */
public final class MineralRoll {
    private static final long SALT = 0x4D494E45524C4C21L;

    private MineralRoll() {
    }

    /**
     * Rolls for one block and one ore. Different ores roll independently at the same position.
     *
     * @param ore id of the ore
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @return uniform value in [0, 1); the block becomes ore if it is below the ore's share
     */
    public static double roll(String ore, int x, int y, int z) {
        long seed = SALT ^ SplitMix64.mix(ore.hashCode()) ^ y;
        return (SplitMix64.hash(seed, x, z) >>> 11) * 0x1.0p-53;
    }
}
