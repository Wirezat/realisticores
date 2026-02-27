package wirezat.realisticores.worldgen.kimberlite;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

/**
 * Evaluates the kimberlite block type at any world position.
 * Uses grid-based pipe placement and a three-zone carrot cone shape.
 */
public class KimberlitePipeNoise {

    private static final int    WORLD_BOTTOM = -64;
    private static final double NOISE_RANGE  = 0.7;

    private final long             worldSeed;
    private final KimberliteConfig config;

    private final SimplexNoiseSampler edgeNoise;
    private final SimplexNoiseSampler oreNoise;
    private final SimplexNoiseSampler xenolithNoise;

    public KimberlitePipeNoise(long worldSeed, KimberliteConfig config) {
        this.worldSeed = worldSeed;
        this.config    = config;

        CheckedRandom rng = new CheckedRandom(worldSeed ^ 0xB7E151628AED2A6BL);
        rng.skip(512);
        edgeNoise     = new SimplexNoiseSampler(rng); rng.skip(512);
        oreNoise      = new SimplexNoiseSampler(rng); rng.skip(512);
        xenolithNoise = new SimplexNoiseSampler(rng);
    }

    public enum KimberliteType {
        NONE,
        KIMBERLITE,
        KIMBERLITE_BRECCIA,
        DIAMOND_ORE,
        XENOLITH
    }

    /** Returns the block type at the given world position. */
    public KimberliteType sample(int worldX, int worldY, int worldZ) {
        KimberliteConfig.PlacementConfig pl = config.placement;
        KimberliteConfig.ShapeConfig     sh = config.shape;

        int cellX = Math.floorDiv(worldX, pl.gridSize);
        int cellZ = Math.floorDiv(worldZ, pl.gridSize);

        for (int dcx = -1; dcx <= 1; dcx++) {
            for (int dcz = -1; dcz <= 1; dcz++) {
                int  cx = cellX + dcx;
                int  cz = cellZ + dcz;
                long h  = cellHash(cx, cz);

                if ((h & 0xFFFFL) / 65536.0 >= pl.spawnChance) continue;

                double jitterRange = pl.gridSize * pl.jitter;
                double pipeX = cx * pl.gridSize + pl.gridSize * 0.5
                        + (((h >> 16) & 0xFFFFL) / 65536.0 * 2.0 - 1.0) * jitterRange;
                double pipeZ = cz * pl.gridSize + pl.gridSize * 0.5
                        + (((h >> 32) & 0xFFFFL) / 65536.0 * 2.0 - 1.0) * jitterRange;

                double dx   = worldX - pipeX;
                double dz   = worldZ - pipeZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double geomRadius = radiusAtY(worldY);
                if (dist > geomRadius + sh.edgeNoiseStrength) continue;

                double edge = edgeNoise.sample(
                        worldX * sh.edgeNoiseFrequency,
                        worldY * sh.edgeNoiseFrequency * 0.25,
                        worldZ * sh.edgeNoiseFrequency) / NOISE_RANGE;
                double effectiveRadius = geomRadius + edge * sh.edgeNoiseStrength;

                if (dist > effectiveRadius || effectiveRadius <= 0) continue;

                KimberliteConfig.OreConfig ore = config.ore;

                // Diamond ore
                if (worldY <= ore.oreMaxY) {
                    double o = oreNoise.sample(
                            worldX * ore.oreNoiseFrequency,
                            worldY * ore.oreNoiseFrequency * 1.3,
                            worldZ * ore.oreNoiseFrequency);
                    if (o / NOISE_RANGE > (ore.oreThreshold * 2.0 - 1.0)) return KimberliteType.DIAMOND_ORE;
                } else if (worldY <= ore.oreUpperMaxY) {
                    double o = oreNoise.sample(
                            worldX * ore.oreNoiseFrequency,
                            worldY * ore.oreNoiseFrequency * 1.3,
                            worldZ * ore.oreNoiseFrequency);
                    if (o / NOISE_RANGE > (ore.oreUpperThreshold * 2.0 - 1.0)) return KimberliteType.DIAMOND_ORE;
                }

                // Xenolith inclusions (diatreme only, not crater)
                if (worldY < sh.craterBaseY) {
                    double x = xenolithNoise.sample(
                            worldX * ore.xenolithFrequency,
                            worldY * ore.xenolithFrequency,
                            worldZ * ore.xenolithFrequency);
                    if (x / NOISE_RANGE > (ore.xenolithThreshold * 2.0 - 1.0)) return KimberliteType.XENOLITH;
                }

                // Breccia outer ring vs massive kimberlite core
                double normalizedDist = dist / Math.max(effectiveRadius, 0.001);
                if (normalizedDist >= 1.0 - config.zones.brecciaFraction) return KimberliteType.KIMBERLITE_BRECCIA;

                return KimberliteType.KIMBERLITE;
            }
        }

        return KimberliteType.NONE;
    }

    /**
     * Pipe radius at a given Y level (three-zone carrot shape).
     *   Crater   (Y > craterBaseY): linear flare upward.
     *   Diatreme (rootZoneMaxY < Y ≤ craterBaseY): power-law taper.
     *   Root     (Y ≤ rootZoneMaxY): linear taper to rootMinRadius.
     */
    private double radiusAtY(int worldY) {
        KimberliteConfig.ShapeConfig sh = config.shape;
        KimberliteConfig.ZoneConfig  zo = config.zones;

        if (worldY > sh.craterBaseY) {
            double progress = MathHelper.clamp(
                    (worldY - sh.craterBaseY) / (double) sh.craterHeightBlocks, 0.0, 1.0);
            return sh.maxRadius * MathHelper.lerp(progress, 1.0, sh.craterWideningFactor);

        } else if (worldY > zo.rootZoneMaxY) {
            double yFactor = (worldY - WORLD_BOTTOM) / (double)(sh.craterBaseY - WORLD_BOTTOM);
            return sh.maxRadius * Math.pow(MathHelper.clamp(yFactor, 0.0, 1.0), sh.taperPower);

        } else {
            double rootBaseYFactor = (zo.rootZoneMaxY - WORLD_BOTTOM) / (double)(sh.craterBaseY - WORLD_BOTTOM);
            double rootBaseRadius  = sh.maxRadius * Math.pow(rootBaseYFactor, sh.taperPower);

            double rootProgress = MathHelper.clamp(
                    (worldY - WORLD_BOTTOM) / (double)(zo.rootZoneMaxY - WORLD_BOTTOM), 0.0, 1.0);
            return MathHelper.lerp(rootProgress, zo.rootMinRadius, rootBaseRadius);
        }
    }

    /** Deterministic cell hash mixed with world seed. */
    private long cellHash(int cellX, int cellZ) {
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