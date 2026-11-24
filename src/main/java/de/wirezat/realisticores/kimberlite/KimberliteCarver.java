package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.ActiveMineral;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes kimberlite pipes, crater bowls and crater lakes into a chunk. */
public final class KimberliteCarver {
    private static final long MINERAL_SALT = 0x4D494E4552414C53L;
    private static final Map<SiteKey, CraterSite> SITES = new ConcurrentHashMap<>();

    private KimberliteCarver() {
    }

    private record SiteKey(long seed, Pipe pipe) {
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
        OreConfig.Resolved config = OreConfig.resolved();
        if (!config.kimberlitePipeActive()) {
            return;
        }
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<Pipe> pipes = Kimberlite.generator().pipesNear(seed, minX, minZ, minX + 15, minZ + 15);
        if (pipes.isEmpty()) {
            return;
        }
        PipeShape shape = Kimberlite.shape();
        Crater crater = Kimberlite.crater();
        int bottomY = Math.max(chunk.getMinBuildHeight(), Kimberlite.params().pipe().bottomY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Pipe pipe : pipes) {
            CraterSite site = SITES.computeIfAbsent(new SiteKey(seed, pipe), key -> CraterSite.measure(pipe, probe));
            int waterY = site.wet() ? crater.waterLevel(site.lowestRimY(), seaLevel) : Integer.MIN_VALUE;
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
                    int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ) - 1;
                    double surfaceWall = shape.normalizedDistance(pipe, x, surfaceY, z);
                    int floorY = surfaceWall <= 1.0 ? crater.floorY(pipe, surfaceWall, site.meanRimY()) : surfaceY;
                    boolean bowl = floorY < surfaceY;
                    if (bowl) {
                        fillBowl(chunk, pos, x, z, floorY, surfaceY, waterY);
                    }
                    carveBody(chunk, pos, pipe, x, z, bottomY, bowl ? floorY : surfaceY, bowl, shape, config.kimberliteMinerals());
                }
            }
        }
    }

    private static void carveBody(ChunkAccess chunk, BlockPos.MutableBlockPos pos, Pipe pipe, int x, int z,
            int bottomY, int groundY, boolean bowl, PipeShape shape, List<ActiveMineral> minerals) {
        FaciesSelector facies = Kimberlite.facies();
        int floorLayers = Kimberlite.params().crater().floorLayers();
        boolean outcrop = shape.isOutcrop(x, z);
        int topY = Math.min(chunk.getMaxBuildHeight() - 1, groundY);
        for (int y = bottomY; y <= topY; y++) {
            double wallDistance = shape.normalizedDistance(pipe, x, y, z);
            if (wallDistance > 1.0) {
                continue;
            }
            pos.set(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !((outcrop || bowl) && isSoil(state))) {
                continue;
            }
            Facies rock = bowl && y > groundY - floorLayers
                    ? Facies.VOLCANICLASTIC
                    : facies.at(pipe, x, y, z, groundY, wallDistance);
            if (rock != Facies.HOST) {
                chunk.setBlockState(pos, mineralBlock(minerals, pipe, rock, x, y, z, blockFor(rock)), false);
            }
        }
    }

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

    private static BlockState mineralBlock(List<ActiveMineral> minerals, Pipe pipe, Facies rock, int x, int y,
            int z, BlockState rockBlock) {
        for (ActiveMineral mineral : minerals) {
            if (!mineral.zones().contains(rock.name())) {
                continue;
            }
            double fraction = mineral.graded()
                    ? mineral.fraction() * Kimberlite.grade().factor(pipe, x, y, z)
                    : mineral.fraction();
            double roll = (SplitMix64.hash(MINERAL_SALT ^ y, x, z) >>> 11) * 0x1.0p-53;
            if (roll < fraction) {
                return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(mineral.block()))
                        .map(block -> block.defaultBlockState())
                        .orElse(rockBlock);
            }
        }
        return rockBlock;
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
