package wirezat.realisticores.worldgen.kimberlite;

/**
 * Manages the KimberlitePipeNoise instance for one world seed.
 * One instance is created per world and cached in the mixin.
 */
public class KimberlitePipeSampler {

    private final KimberlitePipeNoise noise;

    public KimberlitePipeSampler(long worldSeed, KimberliteConfig config) {
        this.noise = new KimberlitePipeNoise(worldSeed, config);
    }

    public KimberlitePipeNoise.KimberliteType sample(int worldX, int worldY, int worldZ) {
        return noise.sample(worldX, worldY, worldZ);
    }
}