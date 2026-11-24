package de.wirezat.realisticores.indicator;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.Tags;

/** The pan: washes sand and gravel at the water's edge and sieves out heavy indicator minerals. */
public class PanItem extends Item {
    private static final int USE_TICKS = 60;
    private static final int SPLASH_INTERVAL = 6;

    /**
     * Creates the pan.
     *
     * @param properties item properties
     */
    public PanItem(Properties properties) {
        super(properties);
    }

    /**
     * Tells whether a block can be panned: loose sediment with water on top or beside it.
     *
     * @param level the world
     * @param pos the block
     * @return true for sand or gravel touching water
     */
    static boolean canPan(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(BlockTags.SAND) && !level.getBlockState(pos).is(Tags.Blocks.GRAVELS)) {
            return false;
        }
        BlockPos above = pos.above();
        if (level.getFluidState(above).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(side)).is(FluidTags.WATER)
                    || level.getFluidState(above.relative(side)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private BlockHitResult target(Level level, Player player) {
        return getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !canPan(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player) || remainingTicks % SPLASH_INTERVAL != 0) {
            return;
        }
        BlockHitResult hit = target(level, player);
        if (hit.getType() != HitResult.Type.BLOCK || !canPan(level, hit.getBlockPos())) {
            player.stopUsingItem();
            return;
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, hit.getLocation().x, hit.getBlockPos().getY() + 1.0,
                    hit.getLocation().z, 6, 0.2, 0.05, 0.2, 0.05);
        }
        level.playSound(null, hit.getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.3F, 1.4F);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof ServerPlayer player)) {
            return stack;
        }
        BlockHitResult hit = target(level, player);
        if (hit.getType() != HitResult.Type.BLOCK || !canPan(level, hit.getBlockPos())
                || !level.mayInteract(player, hit.getBlockPos())) {
            return stack;
        }
        List<ItemStack> found = PanYield.pan(server, hit.getBlockPos(), level.getRandom());
        if (found.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.realisticores.pan.empty"), true);
        } else {
            level.destroyBlock(hit.getBlockPos(), false, player);
        }
        found.forEach(item -> {
            if (!player.getInventory().add(item)) {
                player.drop(item, false);
            }
        });
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return stack;
    }
}
