package wirezat.realisticores.worldgen.coal;

import java.util.ArrayList;
import java.util.List;

public class CoalConfig {

    public GeologyConfig geology        = new GeologyConfig();
    public List<SeamConfig> seams       = new ArrayList<>();
    public List<String> replaceable     = new ArrayList<>();

    public static class GeologyConfig {
        public double regionFrequency   = 0.00035;
        public double regionThreshold   = 0.88;
        public double gasFrequency      = 0.040;
        public double gasThreshold      = 0.95;
    }

    public static class SeamConfig {
        public String id                    = "unnamed";
        public boolean enabled              = true;
        public DepthConfig depth            = new DepthConfig();
        public SizeConfig size              = new SizeConfig();
        public ThicknessConfig thickness    = new ThicknessConfig();
        public TiltConfig tilt              = new TiltConfig();
        public EdgeConfig edge              = new EdgeConfig();
        public CompositionConfig composition = new CompositionConfig();
        public SplitConfig split            = new SplitConfig();

        public static class DepthConfig {
            public int centerY                  = 50;
            public double yVarianceFrequency    = 0.004;
            public double yVarianceAmplitude    = 12;
        }

        public static class SizeConfig {
            public double presenceFrequencyX    = 0.0010;
            public double presenceFrequencyZ    = 0.0010;
            public double presenceThreshold     = 0.70;
            public double secondaryFrequencyX   = 0.005;
            public double secondaryFrequencyZ   = 0.005;
            public double secondaryThreshold    = 0.60;
        }

        public static class ThicknessConfig {
            public double baseMin           = 1;
            public double baseMax           = 8;
            public double noiseFrequency    = 0.010;
        }

        public static class TiltConfig {
            public double angleXDegrees             = 0;
            public double angleZDegrees             = 0;
            public double tiltVarianceFrequency     = 0.003;
            public double tiltVarianceStrength      = 6.0;
        }

        public static class EdgeConfig {
            public double noiseFrequency    = 0.025;
            public double noiseStrength     = 1.5;
        }

        public static class CompositionConfig {
            // Y below which anthracite (coal_block-dominant) generation applies
            public int deepRankY                = 0;
            public double coreThresholdShallow  = 0.15;
            public double coreThresholdDeep     = 0.55;
        }

        public static class SplitConfig {
            public boolean enabled  = false;
            public double frequency = 0.0018;
            public double threshold = 0.65;
            // Width of the rock parting between the two benches
            public double gapSize   = 2.5;
        }
    }
}