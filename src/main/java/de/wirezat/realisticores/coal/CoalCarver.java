package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.kimberlite.TerrainProbe;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes the seams of coal basins, their host rocks and the coaly soil above them into a chunk. */
public final class CoalCarver {
    private static final int SOIL_DEPTH = 4;

    private CoalCarver() {
    }

    /**
     * Writes every basin that touches the chunk.
     *
     * @param chunk the chunk being generated
     * @param seed world seed
     * @param probe reader for the original terrain
     */
    public static void carve(ChunkAccess chunk, long seed, TerrainProbe probe) {
        Map<Rank, String> blocks = OreConfig.resolved().coalBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<ResolvedBasin> basins = Coal.basinsNear(seed, probe, blocks.keySet(), minX, minZ, minX + 15,
                minZ + 15);
        if (basins.isEmpty()) {
            return;
        }
        SeamModel model = Coal.model();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = minX + localX;
                int z = minZ + localZ;
                Optional<SeamColumn> column = model.column(basins, x, z);
                if (column.isPresent() && blocks.containsKey(column.get().rank())) {
                    int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
                    write(chunk, x, z, surfaceY, column.get(), seamBlock(column.get().rank(), blocks),
                            model.isOutcrop(x, z));
                    if (Coal.surface().soil(seed, column.get(), surfaceY, x, z)) {
                        writeSoil(chunk, x, z, surfaceY, column.get().rank() == Rank.LIGNITE);
                    }
                }
            }
        }
    }

    private static void write(ChunkAccess chunk, int x, int z, int surfaceY, SeamColumn column, BlockState seam,
            boolean outcrop) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Layer layer : column.layers()) {
            BlockState rock = layer.rock() == Rock.SEAM ? seam : rockBlock(layer.rock());
            boolean throughSoil = outcrop && layer.rock() == Rock.SEAM;
            int topY = Math.min(layer.maxY(), surfaceY);
            for (int y = Math.max(layer.minY(), chunk.getMinBuildHeight()); y <= topY; y++) {
                BlockState old = chunk.getBlockState(pos.set(x, y, z));
                if (old.is(BlockTags.BASE_STONE_OVERWORLD) || throughSoil && old.is(BlockTags.DIRT)) {
                    chunk.setBlockState(pos, rock, false);
                }
            }
        }
    }

    private static void writeSoil(ChunkAccess chunk, int x, int z, int surfaceY, boolean lignite) {
        Block dirt = lignite ? ModBlocks.LIGNITIC_DIRT.get() : ModBlocks.COALY_DIRT.get();
        Block grass = lignite ? ModBlocks.LIGNITIC_GRASS_BLOCK.get() : ModBlocks.COALY_GRASS_BLOCK.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = surfaceY; y > surfaceY - SOIL_DEPTH; y--) {
            BlockState old = chunk.getBlockState(pos.set(x, y, z));
            if (old.is(Blocks.GRASS_BLOCK)) {
                chunk.setBlockState(pos, grass.withPropertiesOf(old), false);
            } else if (old.is(Blocks.DIRT)) {
                chunk.setBlockState(pos, dirt.defaultBlockState(), false);
            }
        }
    }

    private static BlockState seamBlock(Rank rank, Map<Rank, String> blocks) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(blocks.get(rank)))
                .orElseGet(() -> ownSeam(rank)).defaultBlockState();
    }

    private static Block ownSeam(Rank rank) {
        return switch (rank) {
            case HARD_COAL -> ModBlocks.COAL_SEAM.get();
            case LIGNITE -> ModBlocks.LIGNITE_SEAM.get();
            case ANTHRACITE -> ModBlocks.ANTHRACITE_SEAM.get();
        };
    }

    private static BlockState rockBlock(Rock rock) {
        return switch (rock) {
            case FIRECLAY -> ModBlocks.FIRECLAY.get().defaultBlockState();
            case BLACK_SHALE -> ModBlocks.BLACK_SHALE.get().defaultBlockState();
            case SHALE -> ModBlocks.SHALE.get().defaultBlockState();
            case MUDSTONE -> ModBlocks.MUDSTONE.get().defaultBlockState();
            case SEAM -> throw new IllegalArgumentException("The seam block depends on the rank");
        };
    }
}
