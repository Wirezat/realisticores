package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/** Lets players lay down coal items as pieces on the ground. */
public final class PlacePieces {
    private PlacePieces() {
    }

    /**
     * Lays down one piece when a player uses a coal item on a block that does nothing with the click.
     *
     * @param event the item use event
     */
    public static void onUse(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK || player == null
                || !player.mayUseItemAt(event.getPos(), event.getUseOnContext().getClickedFace(), stack)) {
            return;
        }
        ModBlocks.coalPieces().stream().filter(block -> stack.is(block.item())).findFirst()
                .filter(block -> place(block, new BlockPlaceContext(event.getUseOnContext()), player))
                .ifPresent(block -> event.cancelWithResult(
                        ItemInteractionResult.sidedSuccess(event.getLevel().isClientSide)));
    }

    private static boolean place(CoalPiecesBlock block, BlockPlaceContext context, Player player) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!context.canPlace() || !level.getFluidState(pos).isEmpty()) {
            return false;
        }
        BlockState state = block.getStateForPlacement(context);
        BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
        if (!state.canSurvive(level, pos) || !level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return false;
        }
        if (!level.isClientSide && EventHooks.onBlockPlace(player, before, Direction.UP)) {
            before.restore();
            return false;
        }
        ItemStack stack = context.getItemInHand();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
        }
        SoundType sound = state.getSoundType(level, pos, player);
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
                sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        stack.consume(1, player);
        return true;
    }
}
