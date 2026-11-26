package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;
import java.util.OptionalInt;

/** Decides where a seam leaves loose coal and coaly soil on the surface. */
public final class SurfaceHint {
    private static final long PIECES_SALT = 0x434F414C50494543L;
    private static final long SOIL_SALT = 0x434F414C534F494CL;
    private static final long FRINGE_SALT = 0x434F414C46524E47L;
    private static final int OUTCROP_DEPTH = 3;

    private final CoalParams.SurfaceParams params;

    /**
     * Creates the rule.
     *
     * @param params surface settings
     */
    public SurfaceHint(CoalParams.SurfaceParams params) {
        this.params = params;
    }

    /**
     * Measures how far above the ground the eroded seam of a column lies.
     *
     * @param column layers of the column
     * @param surfaceY Y of the topmost ground block
     * @return 0 at an outcrop, otherwise the blocks up to the lowest seam above the ground, empty without one
     */
    public static OptionalInt height(SeamColumn column, int surfaceY) {
        return column.layers().stream()
                .filter(layer -> layer.rock() == Rock.SEAM && layer.maxY() >= surfaceY - OUTCROP_DEPTH)
                .mapToInt(layer -> Math.max(0, layer.minY() - surfaceY)).min();
    }

    /**
     * Counts the loose pieces of coal on a column.
     *
     * @param seed world seed
     * @param column layers of the column
     * @param surfaceY Y of the topmost ground block
     * @param x world X of the column
     * @param z world Z of the column
     * @return number of pieces, mostly 0
     */
    public int pieces(long seed, SeamColumn column, int surfaceY, int x, int z) {
        OptionalInt height = height(column, surfaceY);
        if (column.rank() == Rank.ANTHRACITE || height.isEmpty() || height.getAsInt() > params.looseMaxHeight()) {
            return 0;
        }
        SplitMix64 random = new SplitMix64(SplitMix64.hash(seed ^ PIECES_SALT, x, z));
        double chance = params.looseDensity() * (1.0 - height.getAsInt() / (params.looseMaxHeight() + 1.0));
        if (random.nextDouble() >= chance) {
            return 0;
        }
        double roll = random.nextDouble();
        return roll < 0.8 ? 1 : roll < 0.95 ? 2 : 3;
    }

    /**
     * Tells whether the soil of a column is mixed with coal.
     *
     * @param seed world seed
     * @param column layers of the column
     * @param surfaceY Y of the topmost ground block
     * @param x world X of the column
     * @param z world Z of the column
     * @return true if the soil is coaly
     */
    public boolean soil(long seed, SeamColumn column, int surfaceY, int x, int z) {
        OptionalInt height = height(column, surfaceY);
        if (column.rank() == Rank.ANTHRACITE || height.isEmpty() || height.getAsInt() > params.soilFringe()) {
            return false;
        }
        double wavelength = params.soilWavelength();
        double kept = 1.0 - height.getAsInt() / (params.soilFringe() + 1.0);
        return ValueNoise.sample2(seed ^ SOIL_SALT, x, z, wavelength) > params.soilThreshold()
                && new SplitMix64(SplitMix64.hash(seed ^ FRINGE_SALT, x, z)).nextDouble() < kept;
    }
}
