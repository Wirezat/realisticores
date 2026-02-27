package wirezat.realisticores.worldgen.kimberlite;

import java.util.ArrayList;
import java.util.List;

/** Config for kimberlite pipe generation. */
public class KimberliteConfig {

    public PlacementConfig placement = new PlacementConfig();
    public ShapeConfig     shape     = new ShapeConfig();
    public ZoneConfig      zones     = new ZoneConfig();
    public OreConfig       ore       = new OreConfig();
    public List<String>    replaceable = new ArrayList<>(List.of(
            "minecraft:stone",
            "minecraft:deepslate",
            "minecraft:granite",
            "minecraft:diorite",
            "minecraft:andesite",
            "minecraft:tuff",
            "minecraft:calcite",
            "minecraft:smooth_basalt",
            "minecraft:gravel",
            "minecraft:dirt",
            "minecraft:cave_air"
    ));

    public static class PlacementConfig {
        /** Grid cell size in blocks. One potential pipe per cell. */
        public int gridSize = 4096;

        /** Probability [0,1] that a cell contains a pipe. */
        public double spawnChance = 0.25;

        /** Jitter [0,1]: max deviation of pipe center from cell center. */
        public double jitter = 0.40;
    }

    public static class ShapeConfig {
        /** Max pipe radius in blocks at craterBaseY. */
        public double maxRadius = 40.0;

        /** Taper exponent. 0.5 = sqrt (realistic), 1.0 = linear. */
        public double taperPower = 0.55;

        /** Y level where the pipe reaches maxRadius (surface). */
        public int craterBaseY = 64;

        /** Crater widening factor above craterBaseY. */
        public double craterWideningFactor = 1.20;

        /** Height in blocks over which crater widening is applied. */
        public int craterHeightBlocks = 16;

        /** 3D edge noise frequency for organic wall irregularity. */
        public double edgeNoiseFrequency = 0.035;

        /** Edge noise strength in blocks (±offset on pipe radius). */
        public double edgeNoiseStrength = 5.0;
    }

    public static class ZoneConfig {
        /** Y below which the root zone begins. */
        public int rootZoneMaxY = -20;

        /** Minimum radius at world bottom (feeder conduit). */
        public double rootMinRadius = 5.0;

        /** Outer breccia fraction [0,1] of the pipe cross-section. */
        public double brecciaFraction = 0.35;
    }

    public static class OreConfig {
        /** Noise frequency for diamond ore pockets. */
        public double oreNoiseFrequency = 0.12;

        /** Sparsity threshold for primary ore zone (below oreMaxY). */
        public double oreThreshold = 0.94;

        /** Max Y for primary (dense) diamond ore zone. */
        public int oreMaxY = -20;

        /** Max Y for secondary (sparse) diamond ore zone. */
        public int oreUpperMaxY = 5;

        /** Sparsity threshold for secondary ore zone. */
        public double oreUpperThreshold = 0.975;

        /** Noise frequency for xenolith inclusions. */
        public double xenolithFrequency = 0.09;

        /** Sparsity threshold for xenolith inclusions (keep high = rare). */
        public double xenolithThreshold = 0.96;
    }
}