package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.indicator.Plume;
import de.wirezat.realisticores.worldgen.LevelTerrainProbe;
import de.wirezat.realisticores.worldgen.Surveys;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** An empty survey map: when used it turns into a map to a point on the indicator trail of a nearby deposit. */
public class TrailMapItem extends Item {
    private static final int MAX_WAIT_TICKS = 1200;
    private static final byte MAP_SCALE = 4;

    private final Target target;
    private final String filledName;

    /** Finds the trail point a map marks. */
    @FunctionalInterface
    public interface Target {
        /**
         * Looks for a trail point near a player, called on the server thread.
         *
         * @param level the overworld
         * @param probe reader for the original terrain
         * @param player the player who uses the map
         * @return future of the point, empty if no deposit or trail was found
         */
        CompletableFuture<Optional<Plume.Spot>> find(ServerLevel level, TerrainProbe probe, Player player);
    }

    /**
     * Creates the map item.
     *
     * @param properties item properties
     * @param target finds the trail point
     * @param filledName translation key of the filled map's name
     */
    public TrailMapItem(Properties properties, Target target, String filledName) {
        super(properties);
        this.target = target;
        this.filledName = filledName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer reader)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        Optional<TerrainProbe> probe = LevelTerrainProbe.of(server);
        if (level.dimension() != Level.OVERWORLD || probe.isEmpty()) {
            playNothingFound(reader);
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, MAX_WAIT_TICKS);
        target.find(server, probe.get(), player)
                .thenApplyAsync(spot -> spot.map(found -> survey(server, found)), Util.backgroundExecutor())
                .whenCompleteAsync((survey, failure) -> {
                    if (player.isRemoved() || !server.getServer().isSameThread()) {
                        return;
                    }
                    player.getCooldowns().removeCooldown(this);
                    if (failure == null) {
                        survey.ifPresentOrElse(found -> hand(server, reader, found), () -> playNothingFound(reader));
                    }
                }, server.getServer())
                .whenComplete(Surveys::logFailure);
        return InteractionResultHolder.consume(stack);
    }

    private record Survey(int x, int z, byte[] outlines) {
    }

    /**
     * Prepares the map of a trail point in the background.
     *
     * @param level the world
     * @param spot the trail point
     * @return the rounded point and the water outlines around it
     */
    private static Survey survey(ServerLevel level, Plume.Spot spot) {
        int x = (int) Math.round(spot.x());
        int z = (int) Math.round(spot.z());
        int blocksPerPixel = 1 << MAP_SCALE;
        int halfSize = BiomePreview.SIZE / 2 * blocksPerPixel;
        return new Survey(x, z,
                BiomePreview.colors(level, snap(x) - halfSize, snap(z) - halfSize, blocksPerPixel));
    }

    /**
     * Rounds a coordinate down to a whole map pixel.
     *
     * @param block world coordinate
     * @return the coordinate of the pixel corner
     */
    private static int snap(int block) {
        int blocksPerPixel = 1 << MAP_SCALE;
        return Math.floorDiv(block, blocksPerPixel) * blocksPerPixel;
    }

    /**
     * Plays the sound of a map that found nothing.
     *
     * @param player the player
     */
    private static void playNothingFound(ServerPlayer player) {
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    /**
     * Swaps one empty survey map of a player for the filled map.
     *
     * @param level the world
     * @param player the player
     * @param survey the surveyed point
     */
    private void hand(ServerLevel level, ServerPlayer player, Survey survey) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack empty = inventory.getItem(slot);
            if (empty.is(this)) {
                empty.consume(1, player);
                ItemStack map = surveyMap(level, survey);
                if (empty.isEmpty()) {
                    inventory.setItem(slot, map);
                } else if (!inventory.add(map)) {
                    player.drop(map, false);
                }
                return;
            }
        }
    }

    /**
     * Builds empty map data centred on a position, rounded to whole map pixels.
     *
     * @param x world X of the centre
     * @param z world Z of the centre
     * @param registries registries used to read and write the map data
     * @return unlocked map data that tracks the player at any distance
     */
    static MapItemSavedData mapData(int x, int z, HolderLookup.Provider registries) {
        CompoundTag tag = MapItemSavedData.createFresh(x, z, MAP_SCALE, true, true, Level.OVERWORLD)
                .save(new CompoundTag(), registries);
        tag.putInt("xCenter", snap(x));
        tag.putInt("zCenter", snap(z));
        return MapItemSavedData.load(tag, registries);
    }

    /**
     * Builds a map whose cross marks a point on an indicator trail. The map follows the player at any distance, so
     * the player marker on the map edge shows the direction to the cross.
     *
     * @param level the world
     * @param survey the marked point and the water outlines around it
     * @return a filled map that fills in as the player explores it
     */
    private ItemStack surveyMap(ServerLevel level, Survey survey) {
        MapItemSavedData data = mapData(survey.x(), survey.z(), level.registryAccess());
        for (int i = 0; i < survey.outlines().length; i++) {
            if (survey.outlines()[i] != 0) {
                data.setColor(i % BiomePreview.SIZE, i / BiomePreview.SIZE, survey.outlines()[i]);
            }
        }
        MapId mapId = level.getFreeMapId();
        level.setMapData(mapId, data);
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        map.set(DataComponents.MAP_ID, mapId);
        MapItemSavedData.addTargetDecoration(map, new BlockPos(survey.x(), 0, survey.z()), "+",
                MapDecorationTypes.RED_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable(filledName));
        return map;
    }
}
