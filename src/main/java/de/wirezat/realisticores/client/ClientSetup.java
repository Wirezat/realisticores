package de.wirezat.realisticores.client;

import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.ModPress;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only registration of screens. */
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
}
