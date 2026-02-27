package wirezat.realisticores.worldgen.coal;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.ArrayList;
import java.util.List;

public class CoalVeinSampler {

    private static final double NOISE_RANGE = 0.7;

    private final SimplexNoiseSampler regionalNoise;
    private final List<CoalVeinNoise> veinNoises = new ArrayList<>();
    private final CoalConfig config;

    public CoalVeinSampler(long worldSeed, CoalConfig config) {
        this.config = config;
        this.regionalNoise = new SimplexNoiseSampler(new CheckedRandom(worldSeed ^ 0xFEEDC0FFEE1234L));

        int index = 0;
        for (CoalConfig.SeamConfig seam : config.seams) {
            if (seam.enabled) {
                veinNoises.add(new CoalVeinNoise(worldSeed, index++, seam, config.geology));
            }
        }
    }

    public CoalVeinNoise.CoalType sample(double worldX, double worldY, double worldZ) {
        double regional = regionalNoise.sample(
                worldX * config.geology.regionFrequency, 0.0, worldZ * config.geology.regionFrequency);

        // Remap [0,1] threshold to noise range [-NOISE_RANGE, NOISE_RANGE]
        if (regional < (config.geology.regionThreshold * 2.0 - 1.0) * NOISE_RANGE)
            return CoalVeinNoise.CoalType.NONE;

        for (CoalVeinNoise vein : veinNoises) {
            CoalVeinNoise.CoalType result = vein.sample(regional, worldX, worldY, worldZ);
            if (result != CoalVeinNoise.CoalType.NONE) return result;
        }

        return CoalVeinNoise.CoalType.NONE;
    }
}