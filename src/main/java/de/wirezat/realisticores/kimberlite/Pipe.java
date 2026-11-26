package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.SplitMix64;

/**
 * One kimberlite pipe.
 *
 * @param x centre X in blocks
 * @param z centre Z in blocks
 * @param diameter surface diameter in blocks
 * @param seed seed of the pipe's shape, rock types and grade, derived from the world seed and the position
 */
public record Pipe(int x, int z, int diameter, long seed) {
    private static final long SALT = 0x504950455F534544L;

    /**
     * Creates the pipe at a position of a world.
     *
     * @param worldSeed world seed
     * @param x centre X in blocks
     * @param z centre Z in blocks
     * @param diameter surface diameter in blocks
     * @return the pipe with its own seed
     */
    public static Pipe of(long worldSeed, int x, int z, int diameter) {
        return new Pipe(x, z, diameter, SplitMix64.hash(worldSeed ^ SALT, x, z));
    }
}
