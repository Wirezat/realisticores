package wirezat.realisticores.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import wirezat.realisticores.ModBlocks;
import wirezat.realisticores.feature.config.KimberliteMagmaFeatureConfig;

public class KimberliteMagmaFeature extends Feature<KimberliteMagmaFeatureConfig> {

    public KimberliteMagmaFeature(Codec<KimberliteMagmaFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<KimberliteMagmaFeatureConfig> context) {
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        KimberliteMagmaFeatureConfig config = context.getConfig();

        int height = config.height;
        float radius = config.radius;
        float oreChance = config.oreChance;
        float noiseIntensity = MathHelper.clamp(config.noise, 0.0f, 1.0f);

        // Seed-basierte Noise-Parameter für konsistente Formen
        long seed = origin.asLong();
        float[] harmonics = new float[8];
        Random noiseRandom = Random.create(seed);
        for (int i = 0; i < harmonics.length; i++) {
            harmonics[i] = noiseRandom.nextFloat() * 100f;
        }

        final float verticalCompression = 0.7f;

        for (int y = 0; y < height; y++) {
            float yFactor = (float) y / height;

            // Die horizontale Verschiebung wurde entfernt, um einen geraden Schlot zu erzeugen.
            float xOffset = 0.0f;
            float zOffset = 0.0f;

            BlockPos currentCenter = origin.add(
                    Math.round(xOffset),
                    y,
                    Math.round(zOffset)
            );

            float baseRadius = radius * (0.75f + yFactor * 0.75f);

            float effectiveRadius = baseRadius * (1.0f + calculateRadialNoise(
                    currentCenter, y, noiseIntensity, harmonics
            ));

            int radiusCeil = MathHelper.ceil(effectiveRadius);
            for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
                for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                    float distanceSq = dx * dx + dz * dz;

                    if (distanceSq > effectiveRadius * effectiveRadius) continue;

                    float distance = MathHelper.sqrt(distanceSq);
                    BlockPos blockPos = currentCenter.add(dx, 0, dz);

                    float edgeFactor = calculateEdgeFactor(
                            blockPos, distance, effectiveRadius, y, noiseIntensity, harmonics
                    );

                    if (distance <= effectiveRadius * (1.0f - edgeFactor)) {
                        BlockState currentState = context.getWorld().getBlockState(blockPos);

                        if (isReplaceable(currentState)) {
                            context.getWorld().setBlockState(blockPos, ModBlocks.KIMBERLITE.getDefaultState(), 2);

                            if (shouldGenerateOre(random, oreChance)) {
                                context.getWorld().setBlockState(
                                        blockPos,
                                        config.oreBlock.get(random, blockPos),
                                        2
                                );
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private float calculateRadialNoise(BlockPos pos, int y, float intensity, float[] harmonics) {
        float noise = 0.3f * MathHelper.sin(pos.getX() * 0.07f + harmonics[0]);
        noise += 0.2f * MathHelper.cos(pos.getZ() * 0.09f + harmonics[1]);
        noise += 0.15f * MathHelper.sin(y * 0.2f + harmonics[2]);
        noise += 0.1f * MathHelper.sin(pos.getX() * 0.03f + pos.getZ() * 0.05f + harmonics[3]);
        return noise * intensity * 0.4f;
    }

    private float calculateEdgeFactor(BlockPos pos, float distance, float radius, int y,
                                      float intensity, float[] harmonics) {
        float normalizedDist = distance / radius;
        float edgeFactor = 0.15f + 0.25f * (1 - normalizedDist);

        float noise = 0.3f * MathHelper.sin(pos.getX() * 0.2f + harmonics[4]);
        noise += 0.25f * MathHelper.cos(pos.getZ() * 0.22f + harmonics[5]);
        noise += 0.2f * MathHelper.sin(y * 0.3f + harmonics[6]);

        return MathHelper.clamp(edgeFactor + noise * intensity * 0.3f, 0.05f, 0.4f);
    }

    private boolean shouldGenerateOre(Random random, float oreChance) {
        return random.nextFloat() < oreChance;
    }

    private boolean isReplaceable(BlockState state) {
        return state.isOf(Blocks.STONE) ||
                state.isOf(Blocks.ANDESITE) ||
                state.isOf(Blocks.DIORITE) ||
                state.isOf(Blocks.GRANITE) ||
                state.isOf(Blocks.DEEPSLATE) ||
                state.isOf(Blocks.TUFF) ||
                state.isOf(Blocks.CALCITE) ||
                state.isOf(Blocks.CAVE_AIR);
    }
}
