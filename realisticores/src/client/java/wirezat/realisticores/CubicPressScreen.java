package wirezat.realisticores;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wirezat.realisticores.blocks.machines.cubicpress.CubicPressScreenHandler;

public final class CubicPressScreen extends HandledScreen<CubicPressScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(RealisticOres.MOD_ID, "textures/gui/cubic_press.png");

    // GUI & texture sizes
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int TEXTURE_SIZE = 256; // atlas size (square)

    // Progress arrow texture coordinates & size
    private static final int PROGRESS_U = 176;
    private static final int PROGRESS_V = 14;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int PROGRESS_X = 79;
    private static final int PROGRESS_Y = 34;

    // Icon (relative to progress position)
    private static final int ICON_U = 176;
    private static final int ICON_V = 0;
    private static final int ICON_WIDTH = 14;
    private static final int ICON_HEIGHT = 14;
    private static final int ICON_OFFSET_X = -22; // icon X relative to PROGRESS_X
    private static final int ICON_OFFSET_Y = 2;    // icon Y relative to PROGRESS_Y

    public CubicPressScreen(CubicPressScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        setGuiSize();

        super.init();

        centerGui();
        centerTitle();
    }

    private void setGuiSize() {
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;
    }

    private void centerGui() {
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    private void centerTitle() {
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int guiX = this.x;
        int guiY = this.y;

        // Hintergrund zeichnen
        context.drawTexture(TEXTURE, guiX, guiY, 0, 0, this.backgroundWidth, this.backgroundHeight, TEXTURE_SIZE, TEXTURE_SIZE);

        // Fortschritt ermitteln und bei Bedarf Fortschrittspfeil + Icon zeichnen
        int progressWidth = this.handler.getScaledProgress();
        if (progressWidth > 0) {
            drawProgress(context, guiX, guiY, progressWidth);
        }
    }

    private void drawProgress(DrawContext context, int guiX, int guiY, int progressWidth) {
        // Fortschrittspfeil (variable Breite)
        context.drawTexture(
                TEXTURE,
                guiX + PROGRESS_X,
                guiY + PROGRESS_Y,
                PROGRESS_U,
                PROGRESS_V,
                progressWidth,
                PROGRESS_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        // neben dem Pfeil
        context.drawTexture(
                TEXTURE,
                guiX + PROGRESS_X + ICON_OFFSET_X,
                guiY + PROGRESS_Y + ICON_OFFSET_Y,
                ICON_U,
                ICON_V,
                ICON_WIDTH,
                ICON_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
