package de.wirezat.realisticores.client;

import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.CrystalPressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Screen of the crystal press. */
public class CrystalPressScreen extends AbstractContainerScreen<CrystalPressMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "textures/gui/crystal_press.png");

    /**
     * Creates the screen.
     *
     * @param menu the press menu
     * @param inventory player inventory
     * @param title title shown above the slots
     */
    public CrystalPressScreen(CrystalPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLit()) {
            int flame = menu.litProgress();
            graphics.blit(TEXTURE, leftPos + 57, topPos + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1);
        }
        int arrow = menu.arrowProgress();
        if (arrow > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, arrow, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
