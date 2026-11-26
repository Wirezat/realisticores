package de.wirezat.realisticores.client;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.coal.CoalyGrassBlock;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.Nullable;

/** Colours coaly grass blocks and the grasses that grow on them. */
final class SoilTint {
    private SoilTint() {
    }

    /**
     * Registers the block colours.
     *
     * @param event the block colour registration event
     */
    static void blocks(RegisterColorHandlersEvent.Block event) {
        for (CoalyGrassBlock block : ModBlocks.coalyGrass()) {
            event.register((state, level, pos, index) -> block.tint(), block);
        }
        event.register((state, level, pos, index) -> plant(level, pos), Blocks.SHORT_GRASS, Blocks.FERN);
        event.register((state, level, pos, index) -> plant(level, root(state, pos)), Blocks.TALL_GRASS,
                Blocks.LARGE_FERN);
    }

    /**
     * Registers the item colours.
     *
     * @param event the item colour registration event
     */
    static void items(RegisterColorHandlersEvent.Item event) {
        for (CoalyGrassBlock block : ModBlocks.coalyGrass()) {
            event.register((stack, index) -> 0xFF000000 | block.tint(), block);
        }
    }

    /**
     * Gives the lower half of a double plant.
     *
     * @param state state of the plant block
     * @param pos position of the plant block
     * @return position of the lower half
     */
    @Nullable
    private static BlockPos root(BlockState state, @Nullable BlockPos pos) {
        return pos != null && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    /**
     * Gives the colour of a grass plant from the soil below it.
     *
     * @param level world being drawn
     * @param pos position of the plant
     * @return tint of coaly grass below, else the biome grass colour
     */
    private static int plant(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return GrassColor.getDefaultColor();
        }
        return level.getBlockState(pos.below()).getBlock() instanceof CoalyGrassBlock soil ? soil.tint()
                : BiomeColors.getAverageGrassColor(level, pos);
    }
}
