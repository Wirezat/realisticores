package de.wirezat.realisticores.client;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Explains on the pan what the vanilla enchantments do there, since they work differently than on tools. */
@EventBusSubscriber(modid = RealisticOres.MOD_ID, value = Dist.CLIENT)
public final class PanTooltips {
    private static final Map<ResourceKey<Enchantment>, String> LINES = Map.of(
            Enchantments.EFFICIENCY, "tooltip.realisticores.pan.efficiency",
            Enchantments.FORTUNE, "tooltip.realisticores.pan.fortune",
            Enchantments.SILK_TOUCH, "tooltip.realisticores.pan.silk_touch");

    private PanTooltips() {
    }

    /**
     * Adds a line for each enchantment of a pan that changes its meaning on the pan.
     *
     * @param event the tooltip event
     */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModItems.IRON_PAN.get())) {
            return;
        }
        event.getItemStack().getEnchantments().keySet().forEach(holder -> holder.unwrapKey()
                .map(LINES::get)
                .ifPresent(key -> event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY))));
    }
}
