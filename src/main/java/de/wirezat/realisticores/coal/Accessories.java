package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;


/** Decides where the minerals that accompany the coal lie: the roll front in sandstone, pyrite nests, seam fires. */
public final class Accessories {
    private static final long FRONT_SALT = 0x524F4C4C46524F4EL;
    private static final long PYRITE_SALT = 0x5059524954454E53L;
    private static final long FIRE_SALT = 0x5345414D46495245L;
    private static final long IRONSTONE_SALT = 0x49524F4E53544F4EL;
    private static final double FRONT_WOBBLE = 0.03;
    private static final double FRONT_WAVELENGTH = 150.0;

    /** State of a sandstone block relative to the roll front. */
    public enum Sand {
        OXIDIZED,
        ORE_BAND,
        REDUCED
    }

    private final CoalParams.AccessoryParams params;

    /**
     * Creates the rules.
     *
     * @param params accessory settings
     */
    public Accessories(CoalParams.AccessoryParams params) {
        this.params = params;
    }

    /**
     * Gives the position of the roll front in a basin.
     *
     * @param basinSeed seed of the basin
     * @param x world X of the column
     * @param z world Z of the column
     * @return distance to the basin centre in basin units beyond which sandstone is oxidized
     */
    public double front(long basinSeed, int x, int z) {
        double share = SplitMix64.unit(SplitMix64.hash(basinSeed ^ FRONT_SALT, 0, 0));
        return params.front().lerp(share)
                + FRONT_WOBBLE * ValueNoise.sample2(basinSeed ^ FRONT_SALT, x, z, FRONT_WAVELENGTH);
    }

    /**
     * Tells how the sandstone of a column relates to the roll front.
     *
     * @param column the column
     * @param x world X of the column
     * @param z world Z of the column
     * @return oxidized outside the front, in the ore band just inside it, reduced further in
     */
    public Sand sandstone(SeamColumn column, int x, int z) {
        double front = front(column.basinSeed(), x, z);
        if (column.edge() > front) {
            return Sand.OXIDIZED;
        }
        return column.edge() > front - params.frontBand() / column.radius() ? Sand.ORE_BAND : Sand.REDUCED;
    }

    /**
     * Rolls whether a black shale block holds pyrite; ore comes in nests of two by two by two cells.
     *
     * @param seed world seed
     * @param fraction share of black shale that becomes pyrite
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @return true if the block is pyrite ore
     */
    public boolean pyrite(long seed, double fraction, int x, int y, int z) {
        return nest(seed ^ PYRITE_SALT, fraction, x, y >> 1, y, z);
    }

    /**
     * Rolls whether a shale or mudstone block holds clay ironstone; ore comes in flat nests two blocks wide and
     * one block high, along the bedding.
     *
     * @param seed world seed
     * @param fraction share of the rock that becomes ironstone
     * @param x world X of the block
     * @param y world Y of the block
     * @param z world Z of the block
     * @return true if the block is ironstone
     */
    public boolean ironstone(long seed, double fraction, int x, int y, int z) {
        return nest(seed ^ IRONSTONE_SALT, fraction, x, y, y, z);
    }

    /**
     * Rolls whether a block belongs to an ore nest two blocks wide; half the blocks of a nest are ore.
     *
     * @param salt seed of the ore
     * @param fraction share of the rock that becomes ore
     * @param x world X of the block
     * @param nestY layer index of the nest, equal for all blocks of one nest
     * @param y world Y of the block
     * @param z world Z of the block
     * @return true if the block is ore
     */
    private static boolean nest(long salt, double fraction, int x, int nestY, int y, int z) {
        double nest = SplitMix64.unit(SplitMix64.hash(salt ^ nestY, x >> 1, z >> 1));
        double block = SplitMix64.unit(SplitMix64.hash(salt ^ ~((long) y), x, z));
        return nest < 2.0 * fraction && block < 0.5;
    }

    /**
     * Tells whether a column lies in a fire patch; patches are discs placed on a grid.
     *
     * @param seed world seed
     * @param x world X of the column
     * @param z world Z of the column
     * @return true inside a fire patch
     */
    public boolean onFire(long seed, int x, int z) {
        int cell = params.fireCell();
        int cellX = Math.floorDiv(x, cell);
        int cellZ = Math.floorDiv(z, cell);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                SplitMix64 random = new SplitMix64(SplitMix64.hash(seed ^ FIRE_SALT, cellX + dx, cellZ + dz));
                if (random.nextDouble() >= params.fireChance()) {
                    continue;
                }
                double centreX = (cellX + dx) * cell + random.nextDouble() * cell;
                double centreZ = (cellZ + dz) * cell + random.nextDouble() * cell;
                int radius = params.fireRadius().pick(random);
                double ox = x + 0.5 - centreX;
                double oz = z + 0.5 - centreZ;
                if (ox * ox + oz * oz <= radius * radius) {
                    return true;
                }
            }
        }
        return false;
    }
}
