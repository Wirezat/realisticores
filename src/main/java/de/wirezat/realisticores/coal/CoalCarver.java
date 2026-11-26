package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.BlockIds;
import de.wirezat.realisticores.ore.MineralRoll;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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
    private static final String SANDSTONE = "SANDSTONE";
    private static final String BLACK_SHALE = "BLACK_SHALE";
    private static final String SEAM_FIRE = "SEAM_FIRE";
    private static final String SHALE = "SHALE";
    private static final String MUDSTONE = "MUDSTONE";
    private static final String BLACKBAND = "BLACKBAND";
    private static final int SOIL_SEARCH = 8;

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
        OreConfig.Resolved config = OreConfig.resolved();
        if (!config.coalBasinActive()) {
            return;
        }
        Map<Rank, String> blocks = config.coalBlocks();
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<ResolvedBasin> basins = Coal.basinsNear(seed, probe, minX, minZ, minX + 15, minZ + 15);
        if (basins.isEmpty()) {
            return;
        }
        SeamModel model = Coal.model();
        Map<String, Accent> accents = accents(config.coalMinerals());
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = minX + localX;
                int z = minZ + localZ;
                Optional<SeamColumn> column = model.column(basins, x, z);
                if (column.isPresent()) {
                    int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
                    boolean coal = config.coalRanks().contains(column.get().rank());
                    Optional<BlockState> seam = seamBlock(column.get().rank(), blocks, coal);
                    write(chunk, seed, x, z, surfaceY, column.get(), seam, model.isOutcrop(seed, x, z), accents);
                    if (burns(chunk, seed, column.get(), x, z, surfaceY)) {
                        burn(chunk, seed, x, z, surfaceY, column.get(), coal ? seam : Optional.empty(),
                                accents.get(SEAM_FIRE));
                    } else if (coal && Coal.surface().soil(seed, column.get(), surfaceY, x, z)) {
                        writeSoil(chunk, x, z, surfaceY, column.get().rank() == Rank.LIGNITE);
                    }
                }
            }
        }
    }

    private record Accent(String ore, BlockState block, double fraction) {
    }

    private static Map<String, Accent> accents(List<ActiveMineral> minerals) {
        Map<String, Accent> accents = new HashMap<>();
        for (ActiveMineral mineral : minerals) {
            for (String zone : mineral.zones()) {
                BlockIds.state(mineral.blockFor(zone)).ifPresent(block ->
                        accents.put(zone, new Accent(mineral.ore(), block, mineral.fraction())));
            }
        }
        return accents;
    }

    private static void write(ChunkAccess chunk, long seed, int x, int z, int surfaceY, SeamColumn column,
            Optional<BlockState> seam, boolean outcrop, Map<String, Accent> accents) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Accessories.Sand sand = Coal.accessories().sandstone(column, x, z);
        int rust = rockTop(chunk, x, z, surfaceY) - Coal.params().accessories().rustDepth();
        for (Layer layer : column.layers()) {
            if (layer.rock() == Rock.SEAM && seam.isEmpty()) {
                continue;
            }
            boolean throughSoil = outcrop && layer.rock() == Rock.SEAM;
            int topY = Math.min(layer.maxY(), surfaceY);
            for (int y = Math.max(layer.minY(), chunk.getMinBuildHeight()); y <= topY; y++) {
                BlockState old = chunk.getBlockState(pos.set(x, y, z));
                if (old.is(BlockTags.BASE_STONE_OVERWORLD) || throughSoil && old.is(BlockTags.DIRT)) {
                    BlockState block = layerBlock(seed, layer.rock(), seam, sand, accents, x, y, z);
                    chunk.setBlockState(pos, y > rust && block.is(ModBlocks.SHALE.get())
                            ? ModBlocks.RUST_STAINED_SHALE.get().defaultBlockState() : block, false);
                }
            }
        }
    }

    private static BlockState layerBlock(long seed, Rock rock, Optional<BlockState> seam, Accessories.Sand sand,
            Map<String, Accent> accents, int x, int y, int z) {
        return switch (rock) {
            case SEAM -> seam.orElseThrow();
            case SANDSTONE -> {
                if (sand == Accessories.Sand.OXIDIZED) {
                    yield Blocks.RED_SANDSTONE.defaultBlockState();
                }
                Accent uranium = accents.get(SANDSTONE);
                yield sand == Accessories.Sand.ORE_BAND && uranium != null
                        && MineralRoll.roll(seed, uranium.ore(), x, y, z) < uranium.fraction()
                        ? uranium.block() : Blocks.SANDSTONE.defaultBlockState();
            }
            case BLACK_SHALE -> {
                Accent pyrite = accents.get(BLACK_SHALE);
                yield pyrite != null && Coal.accessories().pyrite(seed, pyrite.fraction(), x, y, z)
                        ? pyrite.block() : ModBlocks.BLACK_SHALE.get().defaultBlockState();
            }
            case BLACKBAND -> {
                Accent ironstone = accents.get(BLACKBAND);
                yield ironstone != null ? ironstone.block() : ModBlocks.BLACK_SHALE.get().defaultBlockState();
            }
            case FIRECLAY -> ModBlocks.FIRECLAY.get().defaultBlockState();
            case SHALE -> nodule(seed, accents.get(SHALE), ModBlocks.SHALE.get().defaultBlockState(), x, y, z);
            case MUDSTONE -> nodule(seed, accents.get(MUDSTONE), ModBlocks.MUDSTONE.get().defaultBlockState(), x, y, z);
        };
    }

    private static BlockState nodule(long seed, Accent ironstone, BlockState rock, int x, int y, int z) {
        return ironstone != null && Coal.accessories().ironstone(seed, ironstone.fraction(), x, y, z)
                ? ironstone.block() : rock;
    }

    private static int rockTop(ChunkAccess chunk, int x, int z, int surfaceY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = surfaceY; y > surfaceY - SOIL_SEARCH; y--) {
            if (chunk.getBlockState(pos.set(x, y, z)).is(BlockTags.BASE_STONE_OVERWORLD)) {
                return y;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static boolean burns(ChunkAccess chunk, long seed, SeamColumn column, int x, int z, int surfaceY) {
        return column.rank() != Rank.ANTHRACITE && SurfaceHint.height(column, surfaceY).orElse(-1) == 0
                && chunk.getFluidState(new BlockPos(x, surfaceY + 1, z)).isEmpty()
                && Coal.accessories().onFire(seed, x, z);
    }

    private static void burn(ChunkAccess chunk, long seed, int x, int z, int surfaceY, SeamColumn column,
            Optional<BlockState> seam, Accent crust) {
        BlockState burning = (column.rank() == Rank.LIGNITE ? ModBlocks.BURNING_LIGNITE_SEAM
                : ModBlocks.BURNING_COAL_SEAM).get().defaultBlockState();
        BlockState clinker = ModBlocks.CLINKER.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int depth = Coal.params().accessories().fireDepth();
        for (int y = surfaceY; y > surfaceY - depth; y--) {
            BlockState old = chunk.getBlockState(pos.set(x, y, z));
            if (seam.isPresent() && old == seam.get()) {
                chunk.setBlockState(pos, burning, false);
            } else if (old.is(BlockTags.BASE_STONE_OVERWORLD) || old.is(BlockTags.DIRT) || isBasinRock(old)) {
                chunk.setBlockState(pos, clinker, false);
            }
        }
        if (crust != null && chunk.getBlockState(pos.set(x, surfaceY, z)) == clinker
                && MineralRoll.roll(seed, crust.ore(), x, surfaceY, z) < crust.fraction()) {
            chunk.setBlockState(pos, crust.block(), false);
        }
    }

    private static boolean isBasinRock(BlockState state) {
        return state.is(ModBlocks.SHALE.get()) || state.is(ModBlocks.BLACK_SHALE.get())
                || state.is(ModBlocks.RUST_STAINED_SHALE.get()) || state.is(ModBlocks.BLACKBAND_IRONSTONE.get())
                || state.is(ModBlocks.FIRECLAY.get()) || state.is(ModBlocks.MUDSTONE.get())
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE);
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

    private static Optional<BlockState> seamBlock(Rank rank, Map<Rank, String> blocks, boolean coal) {
        Optional<Block> block = Optional.ofNullable(blocks.get(rank)).flatMap(BlockIds::block);
        return (coal ? Optional.of(block.orElseGet(() -> ownSeam(rank))) : block).map(Block::defaultBlockState);
    }

    private static Block ownSeam(Rank rank) {
        return switch (rank) {
            case HARD_COAL -> ModBlocks.COAL_SEAM.get();
            case LIGNITE -> ModBlocks.LIGNITE_SEAM.get();
            case ANTHRACITE -> ModBlocks.ANTHRACITE_SEAM.get();
        };
    }
}
