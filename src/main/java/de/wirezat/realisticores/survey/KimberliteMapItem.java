package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.kimberlite.Field;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** The empty kimberlite map: when used it turns into a map of the nearest kimberlite field. */
public class KimberliteMapItem extends Item {
    private static final int MAX_SEARCH = 40000;
    private static final byte MAP_SCALE = 4;

    /**
     * Creates the map item.
     *
     * @param properties item properties
     */
    public KimberliteMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        Optional<Field> field = level.dimension() == Level.OVERWORLD
                ? Kimberlite.generator().nearestField(server.getSeed(), player.getBlockX(), player.getBlockZ(),
                        MAX_SEARCH)
                : Optional.empty();
        if (field.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.realisticores.kimberlite_map.none"), true);
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(
                ItemUtils.createFilledResult(stack, player, surveyMap(server, field.get())), false);
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
        int blocksPerPixel = 1 << MAP_SCALE;
        tag.putInt("xCenter", Math.floorDiv(x, blocksPerPixel) * blocksPerPixel);
        tag.putInt("zCenter", Math.floorDiv(z, blocksPerPixel) * blocksPerPixel);
        return MapItemSavedData.load(tag, registries);
    }

    /**
     * Builds a map of a kimberlite field. The map shows the indicator trails of the pipes in its area, brighter
     * where grains are more common, and follows the player at any distance, so the player marker on the map edge
     * shows the direction to the field.
     *
     * @param level the world
     * @param field the field to map
     * @return a locked filled map with a biome preview; the trails are painted on as soon as they are computed
     */
    static ItemStack surveyMap(ServerLevel level, Field field) {
        MapId mapId = level.getFreeMapId();
        level.setMapData(mapId, mapData(field.centerX(), field.centerZ(), level.registryAccess()));
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        map.set(DataComponents.MAP_ID, mapId);
        MapItem.renderBiomePreviewMap(level, map);
        MapItem.lockMap(level, map);
        SurveyOverlay.paintAsync(level, map.get(DataComponents.MAP_ID));
        map.set(DataComponents.ITEM_NAME, Component.translatable("item.realisticores.kimberlite_map.filled"));
        return map;
    }
}
