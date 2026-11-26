package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.BlockIds;
import de.wirezat.realisticores.ore.MineralRoll;
import de.wirezat.realisticores.worldgen.ChunkColumns;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.EnumMap;
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
    static final String SEAM_FIRE = "SEAM_FIRE";
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
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<ResolvedBasin> basins = Coal.basinsNear(seed, probe, minX, minZ, minX + 15, minZ + 15);
        if (basins.isEmpty()) {
            return;
        }
        SeamModel model = Coal.model();
        CoalOres.Seams seams = Coal.seams();
        Map<String, Accent> accents = accents(seams.accessories());
        Map<Rank, Optional<BlockState>> seamBlocks = new EnumMap<>(Rank.class);
        for (Rank rank : Rank.values()) {
            seamBlocks.put(rank, seamBlock(rank, seams.blocks(), seams.coal().contains(rank)));
        }
        ChunkColumns.forEach(chunkPos, (localX, localZ, x, z) -> {
            Optional<SeamColumn> column = model.column(basins, x, z);
            if (column.isEmpty()) {
                return false;
            }
            int surfaceY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ);
            boolean coal = seams.coal().contains(column.get().rank());
            Optional<BlockState> seam = seamBlocks.get(column.get().rank());
            write(chunk, seed, x, z, surfaceY, column.get(), seam, model.isOutcrop(seed, x, z), accents);
            if (burns(chunk, seed, column.get(), x, z, surfaceY)) {
                burn(chunk, seed, x, z, surfaceY, column.get(), coal ? seam : Optional.empty(),
                        accents.get(SEAM_FIRE));
            } else if (coal && Coal.surface().soil(seed, column.get(), surfaceY, x, z)) {
                writeSoil(chunk, x, z, surfaceY, column.get().rank() == Rank.LIGNITE);
            }
            return true;
        });
    }

    private record Accent(String ore, BlockState block, double fraction) {
    }

    /**
     * Resolves the blocks of the minerals that accompany the coal, by zone.
     *
     * @param minerals accompanying minerals
     * @return block, ore and share by zone name; minerals whose block is missing are left out
     */
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

    /**
     * Writes the layers of one column.
     *
     * @param chunk the chunk
     * @param seed world seed
     * @param x world X
     * @param z world Z
     * @param surfaceY Y of the surface
     * @param column layers of the column
     * @param seam seam block, empty to keep the rock
     * @param outcrop true if the seam shows through the soil
     * @param accents blocks of the accompanying minerals
     */
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

    /**
     * Picks the block of one layer at a position.
     *
     * @param seed world seed
     * @param rock rock of the layer
     * @param seam seam block
     * @param sand state of the sandstone relative to the roll front
     * @param accents blocks of the accompanying minerals
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return the block to place
     */
    private static BlockState layerBlock(long seed, Rock rock, Optional<BlockState> seam, Accessories.Sand sand,
            Map<String, Accent> accents, int x, int y, int z) {
        return switch (rock) {
            case SEAM -> seam.orElseThrow();
            case SANDSTONE -> {
                if (sand == Accessories.Sand.OXIDIZED) {
                    yield Blocks.RED_SANDSTONE.defaultBlockState();
                }
                Accent uranium = accents.get(Rock.SANDSTONE.name());
                yield sand == Accessories.Sand.ORE_BAND && uranium != null
                        && MineralRoll.roll(seed, uranium.ore(), x, y, z) < uranium.fraction()
                        ? uranium.block() : Blocks.SANDSTONE.defaultBlockState();
            }
            case BLACK_SHALE -> {
                Accent pyrite = accents.get(Rock.BLACK_SHALE.name());
                yield pyrite != null && Coal.accessories().pyrite(seed, pyrite.fraction(), x, y, z)
                        ? pyrite.block() : ModBlocks.BLACK_SHALE.get().defaultBlockState();
            }
            case BLACKBAND -> {
                Accent ironstone = accents.get(Rock.BLACKBAND.name());
                yield ironstone != null ? ironstone.block() : ModBlocks.BLACK_SHALE.get().defaultBlockState();
            }
            case FIRECLAY -> ModBlocks.FIRECLAY.get().defaultBlockState();
            case SHALE -> nodule(seed, accents.get(Rock.SHALE.name()), ModBlocks.SHALE.get().defaultBlockState(), x, y, z);
            case MUDSTONE -> nodule(seed, accents.get(Rock.MUDSTONE.name()), ModBlocks.MUDSTONE.get().defaultBlockState(), x, y, z);
        };
    }

    /**
     * Places clay ironstone nests in shale or mudstone.
     *
     * @param seed world seed
     * @param ironstone ironstone accent, null while siderite is off
     * @param rock the rock
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return ironstone inside a nest, else the rock
     */
    private static BlockState nodule(long seed, Accent ironstone, BlockState rock, int x, int y, int z) {
        return ironstone != null && Coal.accessories().ironstone(seed, ironstone.fraction(), x, y, z)
                ? ironstone.block() : rock;
    }

    /**
     * Finds the top of the bedrock below the soil of a column.
     *
     * @param chunk the chunk
     * @param x world X
     * @param z world Z
     * @param surfaceY Y of the surface
     * @return Y of the highest stone block, {@link Integer#MAX_VALUE} if the soil is deeper than the search
     */
    private static int rockTop(ChunkAccess chunk, int x, int z, int surfaceY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = surfaceY; y > surfaceY - SOIL_SEARCH; y--) {
            if (chunk.getBlockState(pos.set(x, y, z)).is(BlockTags.BASE_STONE_OVERWORLD)) {
                return y;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Tells whether an outcropping seam of a column is on fire.
     *
     * @param chunk the chunk
     * @param seed world seed
     * @param column the column
     * @param x world X
     * @param z world Z
     * @param surfaceY Y of the surface
     * @return true for hard coal or lignite at the surface, dry and inside a fire patch
     */
    private static boolean burns(ChunkAccess chunk, long seed, SeamColumn column, int x, int z, int surfaceY) {
        return column.rank() != Rank.ANTHRACITE && SurfaceHint.height(column, surfaceY).orElse(-1) == 0
                && chunk.getFluidState(new BlockPos(x, surfaceY + 1, z)).isEmpty()
                && Coal.accessories().onFire(seed, x, z);
    }

    /**
     * Sets the top of a column on fire: the seam burns, the rock around it turns to clinker with a crust on top.
     *
     * @param chunk the chunk
     * @param seed world seed
     * @param x world X
     * @param z world Z
     * @param surfaceY Y of the surface
     * @param column the column
     * @param seam seam block, empty if its coal is off
     * @param crust crust accent, null while sal ammoniac is off
     */
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

    /**
     * Tells whether a block is one of the rocks the basin writes.
     *
     * @param state the block
     * @return true for shale, mudstone, fireclay, ironstone and sandstone
     */
    private static boolean isBasinRock(BlockState state) {
        return state.is(ModBlocks.SHALE.get()) || state.is(ModBlocks.BLACK_SHALE.get())
                || state.is(ModBlocks.RUST_STAINED_SHALE.get()) || state.is(ModBlocks.BLACKBAND_IRONSTONE.get())
                || state.is(ModBlocks.FIRECLAY.get()) || state.is(ModBlocks.MUDSTONE.get())
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE);
    }

    /**
     * Turns the dirt and grass of a column into coaly soil.
     *
     * @param chunk the chunk
     * @param x world X
     * @param z world Z
     * @param surfaceY Y of the surface
     * @param lignite true above lignite
     */
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

    /**
     * Resolves the block of a seam.
     *
     * @param rank rank of the seam
     * @param blocks block id by rank
     * @param coal true if the coal of the rank is on
     * @return the configured block, the mod's own seam if a switched-on coal names a missing block, empty to keep the
     *     rock
     */
    private static Optional<BlockState> seamBlock(Rank rank, Map<Rank, String> blocks, boolean coal) {
        Optional<Block> block = Optional.ofNullable(blocks.get(rank)).flatMap(BlockIds::block);
        return (coal ? Optional.of(block.orElseGet(() -> ownSeam(rank))) : block).map(Block::defaultBlockState);
    }

    /**
     * Gives the mod's own seam block of a rank.
     *
     * @param rank the rank
     * @return the seam block
     */
    private static Block ownSeam(Rank rank) {
        return switch (rank) {
            case HARD_COAL -> ModBlocks.COAL_SEAM.get();
            case LIGNITE -> ModBlocks.LIGNITE_SEAM.get();
            case ANTHRACITE -> ModBlocks.ANTHRACITE_SEAM.get();
        };
    }
}
