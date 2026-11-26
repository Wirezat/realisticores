package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.indicator.LevelTerrainProbe;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Scatters loose coal around the outcrops of one chunk. */
public class LooseCoalFeature extends Feature<NoneFeatureConfiguration> {
    /** Creates the feature. */
    public LooseCoalFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        Set<Rank> ranks = OreConfig.resolved().coalRanks();
        Set<Rank> seams = OreConfig.resolved().coalBlocks().keySet();
        if (ranks.isEmpty() || level.getLevel().dimension() != Level.OVERWORLD
                || !(context.chunkGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return false;
        }
        ChunkPos chunk = new ChunkPos(context.origin());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        List<ResolvedBasin> basins = Coal.basinsNear(level.getSeed(),
                new LevelTerrainProbe(level.getLevel(), generator), seams, minX, minZ, minX + 15, minZ + 15);
        boolean placed = false;
        for (int i = 0; i < 256 && !basins.isEmpty(); i++) {
            int x = minX + i % 16;
            int z = minZ + i / 16;
            Optional<SeamColumn> column = Coal.model().column(basins, x, z);
            if (column.isPresent() && ranks.contains(column.get().rank())) {
                placed |= placePieces(level, column.get(), x, z);
            }
        }
        return placed;
    }

    private static boolean placePieces(WorldGenLevel level, SeamColumn column, int x, int z) {
        BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
        int pieces = Coal.surface().pieces(level.getSeed(), column, pos.getY() - 1, x, z);
        BlockState ground = level.getBlockState(pos.below());
        if (pieces == 0 || !level.isEmptyBlock(pos) || ground.is(BlockTags.ICE) || ground.is(BlockTags.SNOW)) {
            return false;
        }
        CoalPiecesBlock block = column.rank() == Rank.LIGNITE ? ModBlocks.LIGNITE_PIECES.get()
                : ModBlocks.COAL_PIECES.get();
        BlockState state = block.defaultBlockState().setValue(CoalPiecesBlock.PIECES, pieces);
        return state.canSurvive(level, pos) && level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
}
