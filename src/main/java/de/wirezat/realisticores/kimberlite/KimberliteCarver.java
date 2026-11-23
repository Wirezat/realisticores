package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Writes kimberlite pipes into a chunk. */
public final class KimberliteCarver {
    private KimberliteCarver() {
    }

    /**
     * Replaces host rock inside every pipe that touches the chunk with kimberlite.
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
        PipeProfile profile = Kimberlite.profile();
        BlockState kimberlite = ModBlocks.KIMBERLITE.get().defaultBlockState();
        int bottomY = Math.max(chunk.getMinBuildHeight(), params.bottomY());
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, params.craterBaseY() + params.craterHeight());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Pipe pipe : pipes) {
            double maxRadius = profile.maxRadius(pipe);
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    double dx = minX + localX - pipe.x();
                    double dz = minZ + localZ - pipe.z();
                    double distanceSquared = dx * dx + dz * dz;
                    if (distanceSquared > maxRadius * maxRadius) {
                        continue;
                    }
                    for (int y = bottomY; y <= topY; y++) {
                        double radius = profile.radiusAt(pipe, y);
                        if (distanceSquared <= radius * radius) {
                            pos.set(minX + localX, y, minZ + localZ);
                            if (isReplaceable(chunk.getBlockState(pos))) {
                                chunk.setBlockState(pos, kimberlite, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.GRAVEL);
    }
}
