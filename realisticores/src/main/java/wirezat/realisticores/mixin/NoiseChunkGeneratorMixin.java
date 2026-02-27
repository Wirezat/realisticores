package wirezat.realisticores.mixin;

import wirezat.realisticores.config.RealisticOresConfigLoader;
import wirezat.realisticores.worldgen.coal.CoalConfig;
import wirezat.realisticores.worldgen.coal.CoalVeinNoise;
import wirezat.realisticores.worldgen.coal.CoalVeinSampler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("RealisticOres");
    @Unique private static final Map<Long, CoalVeinSampler> SAMPLER_CACHE = new ConcurrentHashMap<>();
    @Unique private static volatile Set<Block> replaceableBlocks = null;

    @Inject(
            method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
            at = @At("TAIL")
    )
    private void injectCoalVeins(ChunkRegion region, StructureAccessor structures,
                                 NoiseConfig noiseConfig, Chunk chunk, CallbackInfo ci) {
        long seed = region.toServerWorld().getSeed();

        CoalVeinSampler sampler = SAMPLER_CACHE.computeIfAbsent(seed, s -> {
            CoalConfig config = RealisticOresConfigLoader.get().coal;
            LOGGER.info("[RealisticOres] Creating CoalVeinSampler with {} seam(s)", config.seams.size());
            return new CoalVeinSampler(s, config);
        });

        if (replaceableBlocks == null) {
            synchronized (NoiseChunkGeneratorMixin.class) {
                if (replaceableBlocks == null) {
                    replaceableBlocks = buildReplaceableSet();
                }
            }
        }

        int startX      = chunk.getPos().getStartX();
        int startZ      = chunk.getPos().getStartZ();
        int worldBottom = chunk.getBottomY();
        int worldTop    = chunk.getBottomY() + chunk.getHeight();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int worldX = startX + lx;
                int worldZ = startZ + lz;

                for (int worldY = worldBottom; worldY < worldTop; worldY++) {
                    pos.set(worldX, worldY, worldZ);
                    if (!replaceableBlocks.contains(chunk.getBlockState(pos).getBlock())) continue;

                    CoalVeinNoise.CoalType type = sampler.sample(worldX, worldY, worldZ);
                    switch (type) {
                        case COAL_BLOCK -> chunk.setBlockState(pos, Blocks.COAL_BLOCK.getDefaultState(), false);
                        case COAL_ORE   -> chunk.setBlockState(pos, worldY < 0
                                ? Blocks.DEEPSLATE_COAL_ORE.getDefaultState()
                                : Blocks.COAL_ORE.getDefaultState(), false);
                        case GAS        -> chunk.setBlockState(pos, Blocks.FIRE.getDefaultState(), false);
                        case NONE       -> {}
                    }
                }
            }
        }
    }

    @Unique
    private static Set<Block> buildReplaceableSet() {
        Set<Block> blocks = new HashSet<>();
        for (String id : RealisticOresConfigLoader.get().coal.replaceable) {
            Identifier identifier = new Identifier(id);
            if (Registries.BLOCK.containsId(identifier)) {
                blocks.add(Registries.BLOCK.get(identifier));
            } else {
                LOGGER.warn("[RealisticOres] Unknown block in replaceable list: '{}'", id);
            }
        }
        return blocks;
    }
}