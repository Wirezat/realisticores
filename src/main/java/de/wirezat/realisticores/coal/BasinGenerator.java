package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.Cells;
import de.wirezat.realisticores.worldgen.SplitMix64;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Derives coal basins deterministically from the world seed. */
public final class BasinGenerator {
    private static final long SALT = 0x434F414C42415349L;

    private final CoalParams.BasinParams params;

    /**
     * Creates a generator.
     *
     * @param params generation settings
     */
    public BasinGenerator(CoalParams params) {
        this.params = params.basin();
    }

    /**
     * Builds the basin of one grid cell.
     *
     * @param seed world seed
     * @param cellX grid cell X index
     * @param cellZ grid cell Z index
     * @return the basin, or empty if the cell holds none
     */
    public Optional<Basin> basinAt(long seed, int cellX, int cellZ) {
        long cellSeed = SplitMix64.hash(seed ^ SALT, cellX, cellZ);
        SplitMix64 rng = new SplitMix64(cellSeed);
        if (rng.nextDouble() >= params.chance()) {
            return Optional.empty();
        }
        int x = Cells.center(rng, cellX, params.cellSize(), params.jitter());
        int z = Cells.center(rng, cellZ, params.cellSize(), params.jitter());
        double radius = rng.nextDouble(params.minRadius(), params.maxRadius());
        double root = Math.sqrt(rng.nextDouble(params.minAspect(), 1.0));
        double strike = rng.nextDouble() * Math.PI;
        BasinType type = rng.nextDouble() < params.hardCoalShare() ? BasinType.HARD_COAL : BasinType.LIGNITE;
        return Optional.of(new Basin(x, z, radius / root, radius * root, strike, type, cellSeed));
    }

    /**
     * Lists all basins that may touch a block rectangle.
     *
     * @param seed world seed
     * @param minX west edge of the rectangle in blocks
     * @param minZ north edge of the rectangle in blocks
     * @param maxX east edge of the rectangle in blocks
     * @param maxZ south edge of the rectangle in blocks
     * @return basins whose long axis reaches the rectangle
     */
    public List<Basin> basinsNear(long seed, int minX, int minZ, int maxX, int maxZ) {
        double stretch = 1.0 / (1.0 - params.outlineNoise());
        int reach = (int) Math.ceil(params.maxRadius() / Math.sqrt(params.minAspect()) * stretch
                + params.jitter() * params.cellSize());
        List<Basin> result = new ArrayList<>();
        Cells.around(minX, minZ, maxX, maxZ, reach, params.cellSize(), (cellX, cellZ) ->
                basinAt(seed, cellX, cellZ).filter(basin -> {
                    double r = basin.longRadius() * stretch;
                    return basin.x() + r >= minX && basin.x() - r <= maxX
                            && basin.z() + r >= minZ && basin.z() - r <= maxZ;
                }).ifPresent(result::add));
        return result;
    }
}
