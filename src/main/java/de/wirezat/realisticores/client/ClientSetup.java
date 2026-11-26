package de.wirezat.realisticores.client;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.indicator.PanItem;
import de.wirezat.realisticores.indicator.PanTooltip;
import de.wirezat.realisticores.press.ModPress;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only registration of screens, tooltips and item properties. */
@EventBusSubscriber(modid = RealisticOres.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    /**
     * Registers the crystal press screen.
     *
     * @param event the screen registration event
     */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModPress.PRESS_MENU.get(), CrystalPressScreen::new);
    }

    /**
     * Registers the picture in the pan's tooltip.
     *
     * @param event the tooltip factory registration event
     */
    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PanTooltip.class, ClientPanTooltip::new);
    }

    /**
     * Registers the colours of coaly grass blocks and the plants on them.
     *
     * @param event the block colour registration event
     */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        SoilTint.blocks(event);
    }

    /**
     * Registers the colours of coaly grass block items.
     *
     * @param event the item colour registration event
     */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        SoilTint.items(event);
    }

    /**
     * Registers the item property that switches the pan's model while it holds water.
     *
     * @param event the client setup event
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.IRON_PAN.get(),
                ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "water"),
                (stack, level, entity, seed) -> PanItem.water(stack) > 0 ? 1.0F : 0.0F));
    }
}
