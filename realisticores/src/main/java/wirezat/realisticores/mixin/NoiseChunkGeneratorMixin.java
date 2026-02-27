package wirezat.realisticores.mixin;

import wirezat.realisticores.ModBlocks;
import wirezat.realisticores.config.RealisticOresConfigLoader;
import wirezat.realisticores.worldgen.kimberlite.KimberliteConfig;
import wirezat.realisticores.worldgen.kimberlite.KimberlitePipeNoise;
import wirezat.realisticores.worldgen.kimberlite.KimberlitePipeSampler;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("RealisticOres");

    @Unique private static final Map<Long, KimberlitePipeSampler> KIMBERLITE_SAMPLER_CACHE = new ConcurrentHashMap<>();
    @Unique private static volatile Set<Block> kimberliteReplaceableBlocks = null;

    @Inject(
            method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
            at = @At("TAIL")
    )
    private void injectKimberlite(
            ChunkRegion region,
            StructureAccessor structures,
            NoiseConfig noiseConfig,
            Chunk chunk,
            CallbackInfo ci
    ) {
        long seed = region.toServerWorld().getSeed();
        initKimberlite(seed);

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

                    if (!kimberliteReplaceableBlocks.contains(chunk.getBlockState(pos).getBlock())) continue;

                    KimberlitePipeNoise.KimberliteType type =
                            KIMBERLITE_SAMPLER_CACHE.get(seed).sample(worldX, worldY, worldZ);

                    placeKimberliteBlock(chunk, pos, type);
                }
            }
        }
    }

    @Unique
    private static void placeKimberliteBlock(Chunk chunk, BlockPos.Mutable pos,
                                             KimberlitePipeNoise.KimberliteType type) {
        switch (type) {
            case KIMBERLITE -> chunk.setBlockState(pos,
                    ModBlocks.KIMBERLITE.getDefaultState(), false);

            case KIMBERLITE_BRECCIA -> chunk.setBlockState(pos,
                    // TODO: replace with ModBlocks.KIMBERLITE_BRECCIA once available.
                    ModBlocks.KIMBERLITE.getDefaultState(), false);

            case DIAMOND_ORE -> chunk.setBlockState(pos,
                    ModBlocks.KIMBERLITE_DIAMOND_ORE.getDefaultState(), false);

            case XENOLITH -> chunk.setBlockState(pos,
                    // Calcite as mantle xenolith proxy (garnet/chromite placeholder).
                    // TODO: replace with dedicated xenolith blocks.
                    Blocks.CALCITE.getDefaultState(), false);

            case NONE -> {}
        }
    }

    @Unique
    private static void initKimberlite(long seed) {
        KIMBERLITE_SAMPLER_CACHE.computeIfAbsent(seed, s -> {
            KimberliteConfig config = RealisticOresConfigLoader.get().kimberlite;
            LOGGER.info("[RealisticOres] Creating KimberlitePipeSampler (gridSize={}, spawnChance={})",
                    config.placement.gridSize, config.placement.spawnChance);
            return new KimberlitePipeSampler(s, config);
        });

        if (kimberliteReplaceableBlocks == null) {
            synchronized (NoiseChunkGeneratorMixin.class) {
                if (kimberliteReplaceableBlocks == null) {
                    kimberliteReplaceableBlocks = buildReplaceableSet(
                            RealisticOresConfigLoader.get().kimberlite.replaceable);
                }
            }
        }
    }

    @Unique
    private static Set<Block> buildReplaceableSet(List<String> ids) {
        Set<Block> blocks = new HashSet<>();
        for (String id : ids) {
            Identifier identifier = new Identifier(id);
            if (Registries.BLOCK.containsId(identifier)) {
                blocks.add(Registries.BLOCK.get(identifier));
            } else {
                LOGGER.warn("[RealisticOres] Unknown block in replaceable list: '{}'", id);
            }
        }
        LOGGER.info("[RealisticOres] Kimberlite replaceable blocks: {}",
                blocks.stream().map(b -> Registries.BLOCK.getId(b).toString()).toList());
        return blocks;
    }
}