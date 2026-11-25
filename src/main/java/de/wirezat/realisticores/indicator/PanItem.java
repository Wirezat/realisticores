package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.Plume;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredItem;

/** The pan: washes sediment, measures its indicator grains and collects them. */
public class PanItem extends Item {
    static final int CAPACITY = 1000;
    static final int WASH_COST = 100;

    private static final int USE_TICKS = 60;
    private static final int EFFICIENCY_TICKS = 10;
    private static final int MIN_USE_TICKS = 10;
    private static final int ENCHANTABILITY = 14;
    private static final double TRAIL_SPACING = 2.5;
    private static final int SPLASH_INTERVAL = 6;
    private static final float[] CHIME_PITCHES = {0.8F, 1.0F, 1.3F};
    private static final int CHIME_DELAY_MILLIS = 400;
    private static final int CHIME_LAYERS = 4;
    private static final float CHIME_VOLUME = 5.0F;

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

    /**
     * Gives how much water a pan holds.
     *
     * @param pan the pan
     * @return water in millibuckets, at most one bucket
     */
    public static int water(ItemStack pan) {
        return pan.getOrDefault(ModPan.WATER, SimpleFluidContent.EMPTY).getAmount();
    }

    private static void setWater(ItemStack pan, int amount) {
        pan.set(ModPan.WATER, SimpleFluidContent.copyOf(new FluidStack(Fluids.WATER, amount)));
    }

    private static boolean hasWater(ItemStack pan) {
        return water(pan) >= WASH_COST;
    }

    private static boolean canDip(Level level, Player player, ItemStack pan) {
        if (water(pan) >= CAPACITY) {
            return false;
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        return hit.getType() == HitResult.Type.BLOCK
                && level.getFluidState(hit.getBlockPos()).isSourceOfType(Fluids.WATER)
                && level.mayInteract(player, hit.getBlockPos());
    }

    private static void playFill(Player player) {
        player.playSound(SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
    }

    /**
     * Pours water from a container into a pan. A container that only gives its whole content loses the surplus.
     *
     * @param pan the pan to fill
     * @param source a single fluid container, for example a water bucket
     * @return the container after pouring, or empty if nothing was poured
     */
    static ItemStack pour(ItemStack pan, ItemStack source) {
        IFluidHandlerItem handler = source.getCapability(Capabilities.FluidHandler.ITEM);
        int missing = CAPACITY - water(pan);
        if (handler == null || source.getCount() != 1 || missing == 0) {
            return ItemStack.EMPTY;
        }
        FluidStack poured = handler.drain(new FluidStack(Fluids.WATER, missing), FluidAction.EXECUTE);
        if (poured.isEmpty()) {
            poured = handler.drain(new FluidStack(Fluids.WATER, CAPACITY), FluidAction.EXECUTE);
        }
        if (poured.isEmpty()) {
            return ItemStack.EMPTY;
        }
        setWater(pan, Math.min(CAPACITY, water(pan) + poured.getAmount()));
        return handler.getContainer();
    }

    private static boolean pour(ItemStack pan, ItemStack source, Slot slot, ClickAction action, Player player,
            Predicate<ItemStack> replaceSource) {
        if (action != ClickAction.SECONDARY || source.is(pan.getItem()) || !slot.allowModification(player)) {
            return false;
        }
        ItemStack container = pour(pan, source.copy());
        if (container.isEmpty() || !replaceSource.test(container)) {
            return false;
        }
        playFill(player);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pan, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        return pour(pan, other, slot, action, player, access::set);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pan, Slot slot, ClickAction action, Player player) {
        return pour(pan, slot.getItem(), slot, action, player, container -> {
            slot.set(container);
            return true;
        });
    }

    private BlockHitResult target(Level level, Player player) {
        return getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || canDip(context.getLevel(), player, context.getItemInHand())
                || !canPan(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.PASS;
        }
        if (!hasWater(context.getItemInHand())) {
            context.getLevel().playSound(player, player.blockPosition(), SoundEvents.COPPER_HIT, SoundSource.PLAYERS,
                    0.7F, 0.6F);
            return InteractionResult.FAIL;
        }
        if (context.getLevel() instanceof ServerLevel server) {
            PanYield.measure(server, context.getClickedPos()).whenComplete(Kimberlite::logFailure);
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pan = player.getItemInHand(hand);
        if (!canDip(level, player, pan)) {
            return InteractionResultHolder.pass(pan);
        }
        setWater(pan, CAPACITY);
        playFill(player);
        return InteractionResultHolder.sidedSuccess(pan, level.isClientSide);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new PanTooltip(water(stack) * 100 / CAPACITY, Optional.ofNullable(stack.get(ModPan.LAST)),
                stack.getOrDefault(ModPan.TONE, PyropeTone.PALE), stack.getOrDefault(ModPan.CONTENT, Grains.NONE), 0));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        int efficiency = PanEnchantments.level(entity.level(), Enchantments.EFFICIENCY, stack);
        return Math.max(MIN_USE_TICKS, USE_TICKS - EFFICIENCY_TICKS * efficiency);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
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
                SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, hit.getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.4F, 1.4F);
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
        if (!hasWater(stack)) {
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
        stack.remove(ModPan.TONE);
        setWater(stack, water(stack) - WASH_COST);
        PanYield.measure(server, pos)
                .thenAcceptAsync(result -> collect(server, player, pos, id, result, keepBlock, fortune, reach),
                        server.getServer())
                .whenComplete(Kimberlite::logFailure);
        return stack;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !newStack.is(oldStack.getItem());
    }

    private static void collect(ServerLevel level, ServerPlayer player, BlockPos pos, UUID id,
            PanYield.Measurement result, boolean keepBlock, int fortune, double reach) {
        if (!level.getServer().isSameThread()) {
            return;
        }
        ItemStack pan = findPan(player, id);
        if (pan.isEmpty() || player.isRemoved() || player.serverLevel() != level || !level.isLoaded(pos)
                || !canPan(level, pos)) {
            return;
        }
        if (!keepBlock && CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos,
                level.getBlockState(pos)).isCanceled()) {
            return;
        }
        Grains grains = result.grains();
        Grains sum = pan.getOrDefault(ModPan.CONTENT, Grains.NONE).plus(grains.gain(fortune, keepBlock));
        pan.set(ModPan.LAST, grains);
        pan.set(ModPan.TONE, result.tone());
        pan.set(ModPan.CONTENT, sum.rest());
        if (!keepBlock) {
            level.destroyBlock(pos, false, player);
        }
        Grains items = sum.whole();
        give(player, ModItems.PYROPE, items.pyrope());
        give(player, ModItems.CHROMITE, items.chromite());
        give(player, ModItems.ILMENITE, items.ilmenite());
        if (grains.isEmpty()) {
            level.playSound(null, player.blockPosition(), SoundEvents.MUD_PLACE, SoundSource.PLAYERS, 1.6F, 0.7F);
        } else {
            chime(player, grains);
        }
        if (reach > 0.0 && result.source() != null) {
            TrailEffects.start(player, level, Plume.guide(result.source(), pos.getX() + 0.5, pos.getZ() + 0.5, reach,
                    TRAIL_SPACING));
        }
    }

    private static void chime(ServerPlayer player, Grains grains) {
        int[] amounts = {grains.pyrope(), grains.chromite(), grains.ilmenite()};
        int delay = 0;
        for (int mineral = 0; mineral < amounts.length; mineral++) {
            if (amounts[mineral] > 0) {
                float pitch = CHIME_PITCHES[mineral];
                CompletableFuture.runAsync(() -> playChime(player, pitch),
                        CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS, player.getServer()));
                delay += CHIME_DELAY_MILLIS;
            }
        }
    }

    private static void playChime(ServerPlayer player, float pitch) {
        for (int layer = 0; layer < CHIME_LAYERS; layer++) {
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, CHIME_VOLUME, pitch);
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
