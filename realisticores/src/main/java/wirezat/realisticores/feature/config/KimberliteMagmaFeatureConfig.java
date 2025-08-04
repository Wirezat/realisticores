package wirezat.realisticores.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class KimberliteMagmaFeatureConfig implements FeatureConfig {
    public final int height;
    public final float radius;
    public final float noise;
    public final float oreChance;
    public final BlockStateProvider oreBlock; // Für dein kimberlite_diamond_ore

    public static final Codec<KimberliteMagmaFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("height").forGetter(cfg -> cfg.height),
                    Codec.FLOAT.fieldOf("radius").forGetter(cfg -> cfg.radius),
                    Codec.FLOAT.fieldOf("oreChance").forGetter(cfg -> cfg.oreChance),
                    Codec.FLOAT.optionalFieldOf("noise", 0.3f).forGetter(cfg -> cfg.noise),
                    BlockStateProvider.TYPE_CODEC.fieldOf("ore_block").forGetter(cfg -> cfg.oreBlock)
            ).apply(instance, KimberliteMagmaFeatureConfig::new)
    );

    public KimberliteMagmaFeatureConfig(int height, float radius, float noise, float oreChance, BlockStateProvider oreBlock) {
        this.height = height;
        this.radius = radius;
        this.noise = noise;
        this.oreBlock = oreBlock;
        this.oreChance = oreChance;
    }
}
