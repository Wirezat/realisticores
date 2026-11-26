package de.wirezat.realisticores.coal;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

/** Dirt mixed with coal that can be worked like vanilla dirt. */
public class CoalyDirtBlock extends Block {
    /**
     * Creates the block.
     *
     * @param properties block properties
     */
    public CoalyDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability,
            boolean simulate) {
        BlockState worked = worked(context, ability);
        return worked != null ? worked : super.getToolModifiedState(state, context, ability, simulate);
    }

    /**
     * Gives the block a hoe or shovel makes of the soil.
     *
     * @param context use of the tool on the soil
     * @param ability what the tool does
     * @return farmland or a path, null if the tool does not change the soil
     */
    @Nullable
    static BlockState worked(UseOnContext context, ItemAbility ability) {
        if (!context.getItemInHand().canPerformAction(ability)) {
            return null;
        }
        if (ability == ItemAbilities.SHOVEL_FLATTEN) {
            return Blocks.DIRT_PATH.defaultBlockState();
        }
        boolean open = context.getLevel().getBlockState(context.getClickedPos().above()).isAir();
        return ability == ItemAbilities.HOE_TILL && open ? Blocks.FARMLAND.defaultBlockState() : null;
    }
}
