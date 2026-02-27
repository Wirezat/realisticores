package wirezat.realisticores.worldgen.coal;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

/**
 * Holds all noise samplers for a single coal seam and evaluates
 * whether a given world position falls inside that seam.
 *
 * New in this version:
 *  - Lateral seam splitting (two benches with an interburden gap)
 *  - Methane gas pocket inclusions (GAS type)
 *  - Depth-based coal rank (coreThresholdShallow vs coreThresholdDeep)
 */
public class CoalVeinNoise {

    // Empirically measured practical output range of SimplexNoiseSampler.
    private static final double NOISE_RANGE = 0.7;

    private final SimplexNoiseSampler presenceNoise;
    private final SimplexNoiseSampler secondaryNoise;
    private final SimplexNoiseSampler thicknessNoise;
    private final SimplexNoiseSampler yVarianceNoise;
    private final SimplexNoiseSampler tiltVarianceNoise;
    private final SimplexNoiseSampler edgeNoise;
    private final SimplexNoiseSampler splitNoise;
    private final SimplexNoiseSampler gasNoise;

    private final CoalConfig.SeamConfig config;
    private final CoalConfig.GeologyConfig geologyConfig;
    private final double slopeX;
    private final double slopeZ;

    /**
     * Creates noise samplers for one seam.
     *
     * @param geologyConfig Passed from the top-level config; contains gas settings
     *                      that are shared across all seams (one gas noise per seam,
     *                      but controlled by global geology parameters).
     */
    public CoalVeinNoise(long worldSeed, int veinIndex, CoalConfig.SeamConfig config,
                         CoalConfig.GeologyConfig geologyConfig) {
        this.config        = config;
        this.geologyConfig = geologyConfig;

        long base = worldSeed ^ (0xC3A7F19D5E2B8461L * (long)(veinIndex + 1));
        CheckedRandom rng = new CheckedRandom(base);

        presenceNoise     = new SimplexNoiseSampler(rng); rng.skip(512);
        secondaryNoise    = new SimplexNoiseSampler(rng); rng.skip(512);
        thicknessNoise    = new SimplexNoiseSampler(rng); rng.skip(512);
        yVarianceNoise    = new SimplexNoiseSampler(rng); rng.skip(512);
        tiltVarianceNoise = new SimplexNoiseSampler(rng); rng.skip(512);
        edgeNoise         = new SimplexNoiseSampler(rng); rng.skip(512);
        splitNoise        = new SimplexNoiseSampler(rng); rng.skip(512);
        gasNoise          = new SimplexNoiseSampler(rng);

        slopeX = Math.tan(Math.toRadians(config.tilt.angleXDegrees));
        slopeZ = Math.tan(Math.toRadians(config.tilt.angleZDegrees));
    }

    public enum CoalType {
        NONE,
        COAL_ORE,
        COAL_BLOCK,
        /**
         * Methane gas pocket. The caller is responsible for placing the
         * appropriate block (currently minecraft:fire as placeholder;
         * should be a dedicated gas block in a future version).
         */
        GAS
    }

    /**
     * Converts a sparsity threshold [0,1] to a raw noise cutoff.
     * 0.0 → everything passes | 0.5 → ~50% passes | 1.0 → nothing passes.
     */
    private static double toCutoff(double sparsity) {
        return (sparsity * 2.0 - 1.0) * NOISE_RANGE;
    }

    /**
     * Returns the block type for the given world position within this seam.
     *
     * @param regionalNorm Pre-normalized regional noise value [0,1] from CoalVeinSampler.
     */
    public CoalType sample(double regionalNorm, double worldX, double worldY, double worldZ) {
        CoalConfig.SeamConfig.SizeConfig        sz   = config.size;
        CoalConfig.SeamConfig.DepthConfig       dep  = config.depth;
        CoalConfig.SeamConfig.ThicknessConfig   thk  = config.thickness;
        CoalConfig.SeamConfig.TiltConfig        tlt  = config.tilt;
        CoalConfig.SeamConfig.EdgeConfig        edg  = config.edge;
        CoalConfig.SeamConfig.SplitConfig       spl  = config.split;
        CoalConfig.SeamConfig.CompositionConfig comp = config.composition;

        // ── Presence check ────────────────────────────────────────────────────────
        double presence = presenceNoise.sample(
                worldX * sz.presenceFrequencyX, 0.0, worldZ * sz.presenceFrequencyZ);
        if (presence < toCutoff(sz.presenceThreshold)) return CoalType.NONE;

        // ── Secondary check: lenses and discontinuities ───────────────────────────
        if (sz.secondaryThreshold > 0.0) {
            double secondary = secondaryNoise.sample(
                    worldX * sz.secondaryFrequencyX, 0.0, worldZ * sz.secondaryFrequencyZ);
            if (secondary < toCutoff(sz.secondaryThreshold)) return CoalType.NONE;
        }

        // ── Y center line with smooth vertical drift ──────────────────────────────
        double yVariance = yVarianceNoise.sample(
                worldX * dep.yVarianceFrequency, 0.0, worldZ * dep.yVarianceFrequency)
                * dep.yVarianceAmplitude;
        double centerY = dep.centerY + yVariance;

        // ── Tilt: constant dip + local folding variance ───────────────────────────
        double tiltOffset   = worldX * slopeX + worldZ * slopeZ;
        double tiltVariance = tiltVarianceNoise.sample(
                worldX * tlt.tiltVarianceFrequency, 0.0, worldZ * tlt.tiltVarianceFrequency)
                * tlt.tiltVarianceStrength;
        double effectiveCenterY = centerY + tiltOffset + tiltVariance;

        // ── Thickness: interpolated from noise ────────────────────────────────────
        double tNorm = thicknessNoise.sample(worldX * thk.noiseFrequency, 0.0, worldZ * thk.noiseFrequency);
        double halfThickness = Math.max(
                (thk.baseMin + (tNorm + NOISE_RANGE) / (2.0 * NOISE_RANGE) * (thk.baseMax - thk.baseMin)) / 2.0,
                0.5);

        // ── Edge noise: organic, irregular seam boundary ──────────────────────────
        double edgeVariance   = edgeNoise.sample(
                worldX * edg.noiseFrequency,
                worldY * edg.noiseFrequency * 2.5,
                worldZ * edg.noiseFrequency) * edg.noiseStrength;
        double distFromCenter = Math.abs(worldY - effectiveCenterY);
        double effectiveHalf  = halfThickness + edgeVariance;

        if (distFromCenter > effectiveHalf) return CoalType.NONE;

        // ── Lateral seam split ────────────────────────────────────────────────────
        // When active, removes blocks near the center line to create two benches
        // with a host-rock interburden gap ("dirt band" / "parting").
        if (spl.enabled) {
            double splitVal = splitNoise.sample(worldX * spl.frequency, 0.0, worldZ * spl.frequency);
            if (splitVal > toCutoff(spl.threshold)) {
                // Within a split zone: carve out the central gap
                double gapHalf = spl.gapSize / 2.0;
                if (distFromCenter < gapHalf) return CoalType.NONE;
            }
        }

        // ── Methane gas pockets ───────────────────────────────────────────────────
        // 3D noise creates isolated pockets of trapped methane inside the seam.
        // Evaluated before coal classification so gas overrides ore/block.
        // NOTE: Currently placed as minecraft:fire (placeholder).
        //       Replace with a dedicated "coal_gas" block for full realism.
        double gas = gasNoise.sample(
                worldX * geologyConfig.gasFrequency,
                worldY * geologyConfig.gasFrequency * 1.5,
                worldZ * geologyConfig.gasFrequency);
        if (gas > toCutoff(geologyConfig.gasThreshold)) return CoalType.GAS;

        // ── Depth-based coal rank ─────────────────────────────────────────────────
        // Real coal rank increases with burial depth and metamorphic heat:
        //   Y >= deepRankY  →  Lignite / Bituminous  →  coal_ore dominant
        //   Y <  deepRankY  →  Anthracite            →  coal_block dominant
        double coreThreshold = worldY < comp.deepRankY
                ? comp.coreThresholdDeep
                : comp.coreThresholdShallow;

        double relativeDepth = distFromCenter / Math.max(effectiveHalf, 0.001);
        return relativeDepth < coreThreshold ? CoalType.COAL_BLOCK : CoalType.COAL_ORE;
    }
}