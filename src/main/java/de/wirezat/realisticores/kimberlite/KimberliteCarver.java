package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes kimberlite pipes into a chunk; soil on top stays except at outcrops. */
public final class KimberliteCarver {
    private KimberliteCarver() {
    }

    /**
     * Replaces host rock inside every pipe that touches the chunk with the rock type of its position.
     *
     * @param chunk the chunk being generated
     * @param seed world seed
     */
    public static void carve(ChunkAccess chunk, long seed) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<Pipe> pipes = Kimberlite.generator().pipesNear(seed, minX, minZ, minX + 15, minZ + 15);
        if (pipes.isEmpty()) {
            return;
        }
        KimberliteParams.PipeParams params = Kimberlite.params().pipe();
        PipeShape shape = Kimberlite.shape();
        FaciesSelector facies = Kimberlite.facies();
        int bottomY = Math.max(chunk.getMinBuildHeight(), params.bottomY());
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, params.craterBaseY() + params.craterHeight());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Pipe pipe : pipes) {
            double reach = shape.reach(pipe);
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    int x = minX + localX;
                    int z = minZ + localZ;
                    double dx = x - pipe.x();
                    double dz = z - pipe.z();
                    if (dx * dx + dz * dz > reach * reach) {
                        continue;
                    }
                    int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, localZ) - 1;
                    boolean outcrop = shape.isOutcrop(x, z);
                    for (int y = bottomY; y <= topY; y++) {
                        double wallDistance = shape.normalizedDistance(pipe, x, y, z);
                        if (wallDistance > 1.0) {
                            continue;
                        }
                        pos.set(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !(outcrop && isSoil(state))) {
                            continue;
                        }
                        Facies rock = facies.at(pipe, x, y, z, surfaceY, wallDistance);
                        if (rock != Facies.HOST) {
                            chunk.setBlockState(pos, blockFor(rock), false);
                        }
                    }
                }
            }
        }
    }

    private static BlockState blockFor(Facies rock) {
        return switch (rock) {
            case KIMBERLITE -> ModBlocks.KIMBERLITE.get().defaultBlockState();
            case BRECCIA -> ModBlocks.KIMBERLITE_BRECCIA.get().defaultBlockState();
            case VOLCANICLASTIC -> ModBlocks.VOLCANICLASTIC_KIMBERLITE.get().defaultBlockState();
            case WEATHERED -> ModBlocks.WEATHERED_KIMBERLITE.get().defaultBlockState();
            case SERPENTINIZED -> ModBlocks.SERPENTINIZED_KIMBERLITE.get().defaultBlockState();
            case PERIDOTITE -> ModBlocks.PERIDOTITE.get().defaultBlockState();
            case HOST -> throw new IllegalArgumentException("HOST keeps the original block");
        };
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL);
    }
}
