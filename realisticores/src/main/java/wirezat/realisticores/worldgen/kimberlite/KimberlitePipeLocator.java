package wirezat.realisticores.worldgen.kimberlite;

import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Computes all kimberlite pipe centers deterministically at world load. */
public class KimberlitePipeLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger("RealisticOres");

    /** Grid cells to scan in each direction from the origin. */
    private static final int SEARCH_RADIUS_CELLS = 12;

    public record PipeLocation(int x, int z, int cellX, int cellZ) {
        public String tpCommand() {
            return String.format("/tp @s %d 64 %d", x, z);
        }
    }

    /** Scans all grid cells in the search area and returns located pipe centers. */
    public static List<PipeLocation> locate(long worldSeed, KimberliteConfig config) {
        KimberliteConfig.PlacementConfig pl = config.placement;
        List<PipeLocation> pipes = new ArrayList<>();

        for (int dcx = -SEARCH_RADIUS_CELLS; dcx <= SEARCH_RADIUS_CELLS; dcx++) {
            for (int dcz = -SEARCH_RADIUS_CELLS; dcz <= SEARCH_RADIUS_CELLS; dcz++) {
                long h = cellHash(worldSeed, dcx, dcz);

                if ((h & 0xFFFFL) / 65536.0 >= pl.spawnChance) continue;

                double jitterRange = pl.gridSize * pl.jitter;
                int pipeX = (int)(dcx * pl.gridSize + pl.gridSize * 0.5
                        + (((h >> 16) & 0xFFFFL) / 65536.0 * 2.0 - 1.0) * jitterRange);
                int pipeZ = (int)(dcz * pl.gridSize + pl.gridSize * 0.5
                        + (((h >> 32) & 0xFFFFL) / 65536.0 * 2.0 - 1.0) * jitterRange);

                pipes.add(new PipeLocation(pipeX, pipeZ, dcx, dcz));
            }
        }

        return pipes;
    }

    /** Logs all located pipes as TP commands. Call once on world load. */
    public static void logAll(long worldSeed, KimberliteConfig config) {
        List<PipeLocation> pipes = locate(worldSeed, config);

        LOGGER.info("[RealisticOres] ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LOGGER.info("[RealisticOres] {} kimberlite pipe(s) found within ±{} cells (±{} blocks):",
                pipes.size(), SEARCH_RADIUS_CELLS, SEARCH_RADIUS_CELLS * config.placement.gridSize);

        for (int i = 0; i < pipes.size(); i++) {
            PipeLocation p = pipes.get(i);
            LOGGER.info("[RealisticOres]   #{} cell [{}, {}]  →  {}",
                    i + 1, p.cellX(), p.cellZ(), p.tpCommand());
        }

        LOGGER.info("[RealisticOres] ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /** Must stay in sync with KimberlitePipeNoise.cellHash(). */
    private static long cellHash(long worldSeed, int cellX, int cellZ) {
        long h = worldSeed;
        h ^= (long) cellX * 0x9E3779B97F4A7C15L;
        h ^= (long) cellZ * 0x6C62272E07BB0142L;
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }
}