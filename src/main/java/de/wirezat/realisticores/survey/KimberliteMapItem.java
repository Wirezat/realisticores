package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.kimberlite.Field;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;

/** The empty kimberlite map: when used it turns into a map of the nearest kimberlite field. */
public class KimberliteMapItem extends Item {
    private static final int MAX_SEARCH = 40000;
    private static final byte MAP_SCALE = 5;

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
     * Builds a map of a kimberlite field. The map shows the indicator trails of the pipes in its area, brighter
     * where grains are more common, and follows the player at any distance, so the player marker on the map edge
     * shows the direction to the field.
     *
     * @param level the world
     * @param field the field to map
     * @return a locked filled map with a biome preview; the trails are painted on as soon as they are computed
     */
    static ItemStack surveyMap(ServerLevel level, Field field) {
        ItemStack map = MapItem.create(level, field.centerX(), field.centerZ(), MAP_SCALE, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItem.lockMap(level, map);
        SurveyOverlay.paintAsync(level, map.get(DataComponents.MAP_ID));
        map.set(DataComponents.ITEM_NAME, Component.translatable("item.realisticores.kimberlite_map.filled"));
        return map;
    }
}
