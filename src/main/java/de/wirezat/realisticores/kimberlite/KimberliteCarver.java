package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.BlockIds;
import de.wirezat.realisticores.ore.MineralRoll;
import de.wirezat.realisticores.worldgen.ChunkColumns;
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
            int waterY = crater.hasLake(site.wet())
                    ? crater.waterLevel(site.lowestRimY(), seaLevel) : Integer.MIN_VALUE;
            double reach = shape.reach(pipe);
            PipeShape.Outline outline = shape.outline(pipe);
            PipeCarve carve = new PipeCarve(seed, shape, outline, bottomY, ores);
            ChunkColumns.forEach(chunkPos, (localX, localZ, x, z) -> {
                double dx = x - pipe.x();
                double dz = z - pipe.z();
                if (dx * dx + dz * dz > reach * reach) {
                    return false;
                }
                int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
                double surfaceWall = shape.normalizedDistance(outline, x, surfaceY, z);
                int floorY = surfaceWall <= 1.0
                        ? crater.floorY(pipe, surfaceWall, site.meanRimY(), surfaceY) : surfaceY;
                boolean bowl = floorY < surfaceY;
                Soil soil = null;
                if (bowl) {
                    BlockState top = chunk.getBlockState(pos.set(x, surfaceY, z));
                    BlockState below = chunk.getBlockState(pos.set(x, surfaceY - 1, z));
                    if (isSoil(top)) {
                        BlockState sub = isSoil(below) ? below : Blocks.DIRT.defaultBlockState();
                        soil = new Soil(waterY > floorY ? sub : top, sub);
                    }
                    fillBowl(chunk, pos, x, z, floorY, surfaceY, waterY);
                }
                carveBody(chunk, carve, pos, x, z, bowl ? floorY : surfaceY, bowl, soil);
                return true;
            });
        }
    }

    /**
     * What stays the same while one pipe is written into a chunk.
     *
     * @param seed world seed
     * @param shape shape of the pipes
     * @param outline outline of the pipe
     * @param bottomY lowest Y of the pipe
     * @param ores ore blocks of the pipe
     */
    private record PipeCarve(long seed, PipeShape shape, PipeShape.Outline outline, int bottomY, List<OreBlock> ores) {
    }

    /**
     * Soil of a crater floor.
     *
     * @param top top block
     * @param sub block below the top
     */
    private record Soil(BlockState top, BlockState sub) {
    }

    /**
     * Writes the pipe rock of one column, with soil on an overgrown crater floor.
     *
     * @param chunk the chunk
     * @param carve the pipe being written
     * @param pos reusable position
     * @param x world X
     * @param z world Z
     * @param groundY Y of the ground or the crater floor
     * @param bowl true inside the crater bowl
     * @param soil soil found on the crater floor, null for bare rock; outcrops stay bare
     */
    private static void carveBody(ChunkAccess chunk, PipeCarve carve, BlockPos.MutableBlockPos pos, int x, int z,
            int groundY, boolean bowl, Soil soil) {
        Pipe pipe = carve.outline().pipe();
        PipeShape shape = carve.shape();
        FaciesSelector facies = Kimberlite.facies();
        int floorLayers = Kimberlite.params().crater().floorLayers();
        boolean outcrop = shape.isOutcrop(carve.seed(), x, z);
        Soil cover = outcrop ? null : soil;
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, groundY);
        for (int y = carve.bottomY(); y <= topY; y++) {
            double wallDistance = shape.normalizedDistance(carve.outline(), x, y, z);
            if (wallDistance > 1.0) {
                continue;
            }
            pos.set(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            if (cover != null && y > groundY - Kimberlite.params().crater().soilLayers()) {
                chunk.setBlockState(pos, y == groundY ? cover.top() : cover.sub(), false);
                continue;
            }
            if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !((outcrop || bowl) && isSoil(state))) {
                continue;
            }
            Facies rock = bowl && y > groundY - floorLayers
                    ? Facies.VOLCANICLASTIC
                    : facies.at(pipe, x, y, z, groundY, wallDistance);
            if (rock != Facies.HOST) {
                chunk.setBlockState(pos, mineralBlock(carve.seed(), carve.ores(), pipe, rock, x, y, z, blockFor(rock)),
                        false);
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
