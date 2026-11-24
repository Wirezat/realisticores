package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.Plume;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredItem;

/** The pan: washes sediment, measures its indicator grains and collects them. */
public class PanItem extends Item {
    private static final int USE_TICKS = 60;
    private static final int EFFICIENCY_TICKS = 10;
    private static final int MIN_USE_TICKS = 10;
    private static final int ENCHANTABILITY = 14;
    private static final double TRAIL_SPACING = 2.5;
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
     * Tells whether a block can be panned.
     *
     * @param level the world
     * @param pos the block
     * @return true for sand, gravel and dirt
     */
    static boolean canPan(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModPan.PANNABLE);
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
        if (context.getLevel() instanceof ServerLevel server) {
            PanYield.measure(server, context.getClickedPos()).whenComplete(Kimberlite::logFailure);
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        int efficiency = PanEnchantments.level(entity.level(), Enchantments.EFFICIENCY, stack);
        return Math.max(MIN_USE_TICKS, USE_TICKS - EFFICIENCY_TICKS * efficiency);
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY;
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
        boolean sand = level.getBlockState(hit.getBlockPos()).is(Tags.Blocks.SANDS);
        level.playSound(null, hit.getBlockPos(), sand ? SoundEvents.BRUSH_SAND : SoundEvents.BRUSH_GRAVEL,
                SoundSource.PLAYERS, 0.6F, 1.0F);
        level.playSound(null, hit.getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.15F, 1.4F);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof ServerPlayer player)) {
            return stack;
        }
        BlockHitResult hit = target(level, player);
        if (hit.getType() != HitResult.Type.BLOCK || !canPan(level, hit.getBlockPos())) {
            return stack;
        }
        boolean keepBlock = PanEnchantments.level(level, Enchantments.SILK_TOUCH, stack) > 0;
        if (!keepBlock && !level.mayInteract(player, hit.getBlockPos())) {
            return stack;
        }
        int fortune = PanEnchantments.level(level, Enchantments.FORTUNE, stack);
        double reach = PanEnchantments.trailReach(PanEnchantments.level(level, PanEnchantments.TRAIL_SENSE, stack));
        BlockPos pos = hit.getBlockPos();
        UUID id = stack.getOrDefault(ModPan.ID, UUID.randomUUID());
        stack.set(ModPan.ID, id);
        stack.remove(ModPan.LAST);
        PanYield.measure(server, pos)
                .thenAcceptAsync(result -> collect(server, player, pos, id, result, keepBlock, fortune, reach),
                        server.getServer())
                .whenComplete(Kimberlite::logFailure);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        return stack;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !newStack.is(oldStack.getItem());
    }

    private static void collect(ServerLevel level, ServerPlayer player, BlockPos pos, UUID id,
            PanYield.Measurement result, boolean keepBlock, int fortune, double reach) {
        ItemStack pan = findPan(player, id);
        if (pan.isEmpty() || player.isRemoved() || player.serverLevel() != level || !level.isLoaded(pos)
                || !canPan(level, pos)) {
            return;
        }
        Grains grains = result.grains();
        Grains sum = pan.getOrDefault(ModPan.CONTENT, Grains.NONE).plus(grains.gain(fortune, keepBlock));
        pan.set(ModPan.LAST, grains);
        pan.set(ModPan.CONTENT, sum.rest());
        if (!keepBlock) {
            level.destroyBlock(pos, false, player);
        }
        Grains items = sum.whole();
        give(player, ModItems.PYROPE, items.pyrope());
        give(player, ModItems.CHROMITE, items.chromite());
        give(player, ModItems.ILMENITE, items.ilmenite());
        if (grains.isEmpty()) {
            level.playSound(null, player.blockPosition(), SoundEvents.MUD_PLACE, SoundSource.PLAYERS, 0.8F, 0.7F);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F,
                    1.2F);
        }
        if (reach > 0.0 && result.source() != null) {
            TrailEffects.start(player, level, Plume.guide(result.source(), pos.getX() + 0.5, pos.getZ() + 0.5, reach,
                    TRAIL_SPACING));
        }
    }

    private static ItemStack findPan(ServerPlayer player, UUID id) {
        ItemStack carried = player.containerMenu.getCarried();
        if (id.equals(carried.get(ModPan.ID))) {
            return carried;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (id.equals(stack.get(ModPan.ID))) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void give(ServerPlayer player, DeferredItem<Item> mineral, int count) {
        if (count > 0) {
            ItemStack stack = new ItemStack(mineral.get(), count);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }
}
