package de.wirezat.realisticores.coal;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

/** Grass with a fixed tint that lives only on its own soil. */
public class CoalyGrassBlock extends GrassBlock {
    private final Supplier<? extends Block> soil;
    private final int tint;

    /**
     * Creates the block.
     *
     * @param soil dirt the grass spreads to and turns into without light
     * @param tint colour of the grass as 0xRRGGBB
     * @param properties block properties
     */
    public CoalyGrassBlock(Supplier<? extends Block> soil, int tint, BlockBehaviour.Properties properties) {
        super(properties);
        this.soil = soil;
        this.tint = tint;
    }

    /**
     * Gives the colour of the grass and of the plants on it.
     *
     * @return colour as 0xRRGGBB
     */
    public int tint() {
        return tint;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }
        if (!canLive(state, level, pos)) {
            level.setBlockAndUpdate(pos, soil.get().defaultBlockState());
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (level.getBlockState(target).is(soil.get()) && canLive(defaultBlockState(), level, target)
                    && !level.getFluidState(target.above()).is(FluidTags.WATER)) {
                boolean snowy = level.getBlockState(target.above()).is(Blocks.SNOW);
                level.setBlockAndUpdate(target, defaultBlockState().setValue(SNOWY, snowy));
            }
        }
    }

    /**
     * Tells whether grass survives on a block, like vanilla grass.
     *
     * @param state state of the grass block
     * @param level the world
     * @param pos position of the grass block
     * @return true if light reaches the block through what lies on it
     */
    private static boolean canLive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState cover = level.getBlockState(above);
        if (cover.is(Blocks.SNOW) && cover.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        return cover.getFluidState().getAmount() < 8 && LightEngine.getLightBlockInto(level, state, pos, cover, above,
                Direction.UP, cover.getLightBlock(level, above)) < level.getMaxLightLevel();
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability,
            boolean simulate) {
        BlockState worked = CoalyDirtBlock.worked(context, ability);
        return worked != null ? worked : super.getToolModifiedState(state, context, ability, simulate);
    }
}
