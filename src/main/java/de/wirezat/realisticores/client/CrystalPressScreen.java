package de.wirezat.realisticores.client;

import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.CrystalPressMenu;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Screen of the crystal press: the anvils close when a run starts and the crystal grows between them. */
public class CrystalPressScreen extends AbstractContainerScreen<CrystalPressMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "textures/gui/crystal_press.png");
    private static final int CLOSED = 4;
    private static final int CHAMBER_TOP = 18;
    private static final int CHAMBER_BOTTOM = 68;
    private static final int ANVIL_X = 78;
    private static final int ANVIL_WIDTH = 28;
    private static final int ANVIL_HEIGHT = 17;
    private static final int ROD_X = 86;
    private static final int ROD_WIDTH = 12;
    private static final int GEM_SIZE = 14;
    private static final int GEM_STAGES = 8;
    private static final int GAUGE_HEIGHT = 16;
    private static final int ARROW_WIDTH = 22;
    private static final int FLAME_SIZE = 14;
    private static final double PULSE_MILLIS = 400.0;
    private static final int START_TICKS = 8;

    private int closed;
    private int lastClosed;
    private int lastProgress;

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
    protected void containerTick() {
        super.containerTick();
        int progress = menu.progress();
        boolean runStarted = progress <= START_TICKS && lastProgress > progress + START_TICKS;
        boolean seenMidRun = lastProgress == 0 && progress > START_TICKS;
        if (runStarted || seenMidRun) {
            closed = seenMidRun ? CLOSED : 0;
        }
        lastClosed = closed;
        closed = Mth.clamp(closed + (progress > 0 ? 1 : -1), 0, CLOSED);
        lastProgress = progress;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderChamber(graphics, Math.round(Mth.lerp(partialTick, lastClosed, closed)));
        if (menu.isLit()) {
            int flame = menu.litProgress(FLAME_SIZE);
            sprite(graphics, 44, 36 + FLAME_SIZE - flame, 210, 34 + FLAME_SIZE - flame, FLAME_SIZE, flame);
        }
        int seed = menu.seedProgress(GAUGE_HEIGHT);
        sprite(graphics, 36, 52 + GAUGE_HEIGHT - seed, 204, 4 + GAUGE_HEIGHT - seed, 4, seed);
        sprite(graphics, 114, 36, 176, 56, menu.arrowProgress(ARROW_WIDTH), 15);
    }

    private void renderChamber(GuiGraphics graphics, int offset) {
        if (menu.isLit()) {
            float heat = 0.9F + 0.1F * (float) Math.sin(Util.getMillis() / PULSE_MILLIS);
            graphics.setColor(heat, heat, heat, 1.0F);
            sprite(graphics, 75, 32, 176, 34, 34, 22);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        sprite(graphics, ROD_X, CHAMBER_TOP, 204, 0, ROD_WIDTH, offset);
        sprite(graphics, ROD_X, CHAMBER_BOTTOM - offset, 204, 0, ROD_WIDTH, offset);
        sprite(graphics, ANVIL_X, CHAMBER_TOP + offset, 176, 0, ANVIL_WIDTH, ANVIL_HEIGHT);
        sprite(graphics, ANVIL_X, CHAMBER_BOTTOM - ANVIL_HEIGHT - offset, 176, 17, ANVIL_WIDTH, ANVIL_HEIGHT);
        if (menu.progress() > 0) {
            int stage = Math.min(GEM_STAGES - 1, menu.arrowProgress(GEM_STAGES));
            sprite(graphics, 85, 36, stage * GEM_SIZE, 168, GEM_SIZE, GEM_SIZE);
        }
    }

    private void sprite(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        if (width > 0 && height > 0) {
            graphics.blit(TEXTURE, leftPos + x, topPos + y, u, v, width, height);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
