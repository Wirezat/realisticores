package wirezat.realisticores;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RealisticOresClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Registers your machine's screen handler so the game knows what GUI to open.
        HandledScreens.register(ModBlocks.CUBIC_PRESS_SCREEN_HANDLER_TYPE, CubicPressScreen::new);

        // Registers your mod's custom item groups.
        ModItemGroups.registerModItemGroups();
    }
}
