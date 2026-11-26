package de.wirezat.realisticores.ore;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Lifecycle;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.worldgen.Climate;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.jupiter.api.Test;

class DepositGenerationTest {
    private static final long SEED = 12345L;
    private static final int GROUND_Y = 70;
    private static final int SEA_LEVEL = 63;
    private static final int SEARCH_LIMIT = 100_000;
    private static final double HILL_HEIGHT = 80.0;
    private static final double HILL_WAVELENGTH = 320.0;

    private static final TerrainProbe HILLS = new TerrainProbe() {
        @Override
        public int terrainHeight(int x, int z) {
            return GROUND_Y + (int) (HILL_HEIGHT * Math.sin(x / HILL_WAVELENGTH));
        }

        @Override
        public Climate climate(int x, int z) {
            return Climate.TEMPERATE;
        }

        @Override
        public boolean hasPrecipitation(int x, int y, int z) {
            return true;
        }
    };

    private static void bindGroundTags() {
        BuiltInRegistries.BLOCK.bindTags(Map.of(
                BlockTags.BASE_STONE_OVERWORLD, List.of(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.STONE)),
                BlockTags.DIRT, List.of(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.DIRT),
                        BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.GRASS_BLOCK)),
                BlockTags.SAND, List.of(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.SAND))));
    }

    private static Registry<Biome> plainsOnly() {
        MappedRegistry<Biome> biomes = new MappedRegistry<>(Registries.BIOME, Lifecycle.stable());
        Registry.register(biomes, Biomes.PLAINS,
                VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS).value());
        biomes.freeze();
        return biomes;
    }

    private static ProtoChunk flatChunk(ChunkPos pos, Registry<Biome> biomes) {
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, LevelHeightAccessor.create(-64, 384), biomes, null);
        BlockPos.MutableBlockPos block = new BlockPos.MutableBlockPos();
        for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
            for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                int ground = HILLS.terrainHeight(x, z);
                for (int y = -64; y < ground; y++) {
                    chunk.setBlockState(block.set(x, y, z), Blocks.STONE.defaultBlockState(), false);
                }
                chunk.setBlockState(block.set(x, ground, z), Blocks.GRASS_BLOCK.defaultBlockState(), false);
            }
        }
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
        return chunk;
    }

    private static boolean holdsModBlocks(ProtoChunk chunk) {
        BlockPos.MutableBlockPos block = new BlockPos.MutableBlockPos();
        ChunkPos pos = chunk.getPos();
        for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
            for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                for (int y = -64; y <= HILLS.terrainHeight(x, z); y++) {
                    if (BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(block.set(x, y, z)).getBlock())
                            .getNamespace().equals(RealisticOres.MOD_ID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Test
    void everyLocateTargetLeadsToADepositItsCarverWrites() {
        bindGroundTags();
        Registry<Biome> biomes = plainsOnly();
        for (DepositType deposit : Deposits.ALL) {
            for (LocateTarget target : deposit.locateTargets()) {
                Optional<BlockPos> found = target.search().nearest(SEED, HILLS, BlockPos.ZERO, SEARCH_LIMIT);
                assertTrue(found.isPresent(), target.name() + " not found");
                ProtoChunk chunk = flatChunk(new ChunkPos(found.get()), biomes);
                deposit.carve(chunk, SEED, HILLS, SEA_LEVEL);
                assertTrue(holdsModBlocks(chunk), target.name() + " left its chunk untouched");
            }
        }
    }
}
