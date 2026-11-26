package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.BlockIds;
import de.wirezat.realisticores.ore.MineralRoll;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes kimberlite pipes, crater bowls and crater lakes into a chunk. */
public final class KimberliteCarver {
    private KimberliteCarver() {
    }

    /**
     * Writes every pipe that touches the chunk: the pipe body with its rock types, the crater bowl in the
     * terrain and, in wet biomes, the crater lake.
     *
     * @param chunk the chunk being generated
     * @param seed world seed
     * @param probe reader for the original terrain and climate
     * @param seaLevel sea level of the world
     */
    public static void carve(ChunkAccess chunk, long seed, TerrainProbe probe, int seaLevel) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<Pipe> pipes = Kimberlite.generator().pipesNear(seed, minX, minZ, minX + 15, minZ + 15);
        if (pipes.isEmpty()) {
            return;
        }
        List<OreBlock> ores = oreBlocks(OreConfig.resolved().minerals(KimberlitePipe.ID));
        PipeShape shape = Kimberlite.shape();
        Crater crater = Kimberlite.crater();
        int bottomY = Math.max(chunk.getMinBuildHeight(), Kimberlite.params().pipe().bottomY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Pipe pipe : pipes) {
            CraterSite site = Kimberlite.craterSite(pipe, probe);
            int waterY = crater.hasLake(site.wet()) ? crater.waterLevel(site.lowestRimY(), seaLevel) : Integer.MIN_VALUE;
            double reach = shape.reach(pipe);
            PipeShape.Outline outline = shape.outline(pipe);
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    int x = minX + localX;
                    int z = minZ + localZ;
                    double dx = x - pipe.x();
                    double dz = z - pipe.z();
                    if (dx * dx + dz * dz > reach * reach) {
                        continue;
                    }
                    int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
                    double surfaceWall = shape.normalizedDistance(outline, x, surfaceY, z);
                    int floorY = surfaceWall <= 1.0 ? crater.floorY(pipe, surfaceWall, site.meanRimY(), surfaceY) : surfaceY;
                    boolean bowl = floorY < surfaceY;
                    BlockState topSoil = null;
                    BlockState subSoil = null;
                    if (bowl) {
                        BlockState top = chunk.getBlockState(pos.set(x, surfaceY, z));
                        BlockState below = chunk.getBlockState(pos.set(x, surfaceY - 1, z));
                        if (isSoil(top)) {
                            boolean underWater = waterY > floorY;
                            subSoil = isSoil(below) ? below : Blocks.DIRT.defaultBlockState();
                            topSoil = underWater ? subSoil : top;
                        }
                        fillBowl(chunk, pos, x, z, floorY, surfaceY, waterY);
                    }
                    boolean overgrown = topSoil != null && !shape.isOutcrop(seed, x, z);
                    carveBody(chunk, seed, pos, outline, x, z, bottomY, bowl ? floorY : surfaceY, bowl, shape,
                            ores, overgrown ? topSoil : null, subSoil);
                }
            }
        }
    }

    /**
     * Writes the pipe rock of one column, with soil on an overgrown crater floor.
     *
     * @param chunk the chunk
     * @param seed world seed
     * @param pos reusable position
     * @param outline outline of the pipe
     * @param x world X
     * @param z world Z
     * @param bottomY lowest Y of the pipe
     * @param groundY Y of the ground or the crater floor
     * @param bowl true inside the crater bowl
     * @param shape shape of the pipes
     * @param ores ore blocks of the pipe
     * @param topSoil top soil block, null for bare rock
     * @param subSoil soil block below the top
     */
    private static void carveBody(ChunkAccess chunk, long seed, BlockPos.MutableBlockPos pos,
            PipeShape.Outline outline, int x, int z, int bottomY, int groundY, boolean bowl, PipeShape shape, List<OreBlock> ores,
            BlockState topSoil, BlockState subSoil) {
        Pipe pipe = outline.pipe();
        FaciesSelector facies = Kimberlite.facies();
        int floorLayers = Kimberlite.params().crater().floorLayers();
        boolean outcrop = shape.isOutcrop(seed, x, z);
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, groundY);
        for (int y = bottomY; y <= topY; y++) {
            double wallDistance = shape.normalizedDistance(outline, x, y, z);
            if (wallDistance > 1.0) {
                continue;
            }
            pos.set(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            if (topSoil != null && y > groundY - Kimberlite.params().crater().soilLayers()) {
                chunk.setBlockState(pos, y == groundY ? topSoil : subSoil, false);
                continue;
            }
            if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !((outcrop || bowl) && isSoil(state))) {
                continue;
            }
            Facies rock = bowl && y > groundY - floorLayers
                    ? Facies.VOLCANICLASTIC
                    : facies.at(pipe, x, y, z, groundY, wallDistance);
            if (rock != Facies.HOST) {
                chunk.setBlockState(pos, mineralBlock(seed, ores, pipe, rock, x, y, z, blockFor(rock)), false);
            }
        }
    }

    /**
     * Clears the crater bowl of a column and fills it with water up to the lake level.
     *
     * @param chunk the chunk
     * @param pos reusable position
     * @param x world X
     * @param z world Z
     * @param floorY Y of the crater floor
     * @param surfaceY Y of the original surface
     * @param waterY Y of the lake surface, {@link Integer#MIN_VALUE} without a lake
     */
    private static void fillBowl(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int x, int z, int floorY,
            int surfaceY, int waterY) {
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, Math.max(surfaceY, waterY));
        for (int y = floorY + 1; y <= topY; y++) {
            pos.set(x, y, z);
            if (y <= waterY) {
                if (y <= surfaceY || chunk.getBlockState(pos).isAir()) {
                    chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                }
            } else {
                chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
            }
        }
    }

    private record OreBlock(ActiveMineral mineral, BlockState block) {
    }

    /**
     * Resolves the blocks of the pipe's minerals once per chunk.
     *
     * @param minerals active minerals of the pipe
     * @return minerals with their block; minerals whose block is missing are left out
     */
    private static List<OreBlock> oreBlocks(List<ActiveMineral> minerals) {
        return minerals.stream().flatMap(mineral -> BlockIds.state(mineral.block())
                .map(block -> new OreBlock(mineral, block)).stream()).toList();
    }

    /**
     * Rolls whether a rock block becomes one of the pipe's ores.
     *
     * @param seed world seed
     * @param ores ore blocks of the pipe
     * @param pipe the pipe
     * @param rock rock type of the block
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @param rockBlock block of the rock
     * @return the first ore whose roll hits, else the rock block
     */
    private static BlockState mineralBlock(long seed, List<OreBlock> ores, Pipe pipe, Facies rock, int x, int y, int z,
            BlockState rockBlock) {
        for (OreBlock ore : ores) {
            ActiveMineral mineral = ore.mineral();
            if (!mineral.zones().contains(rock.name())) {
                continue;
            }
            double fraction = mineral.graded()
                    ? mineral.fraction() * Kimberlite.grade().factor(pipe, x, y, z)
                    : mineral.fraction();
            if (MineralRoll.roll(seed, mineral.ore(), x, y, z) < fraction) {
                return ore.block();
            }
        }
        return rockBlock;
    }

    /**
     * Gives the block of a rock type.
     *
     * @param rock the rock type, not HOST
     * @return its block
     */
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

    /**
     * Tells whether a block is loose soil.
     *
     * @param state the block
     * @return true for dirt, sand and gravel
     */
    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL);
    }
}
