package de.wirezat.realisticores.client;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.indicator.Grains;
import de.wirezat.realisticores.indicator.PanItem;
import de.wirezat.realisticores.indicator.PanTooltip;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.joml.Matrix4f;

/** Draws the water of a pan as a bar and its grains as two rows of icons and numbers. */
public final class ClientPanTooltip implements ClientTooltipComponent {
    private static final List<DeferredItem<Item>> MINERALS =
            List.of(ModItems.PYROPE, ModItems.CHROMITE, ModItems.ILMENITE);
    private static final String LAST_KEY = "tooltip.realisticores.pan.last";
    private static final String CONTENT_KEY = "tooltip.realisticores.pan.content";
    private static final ResourceLocation LAST_SYMBOL = sprite("measurement");
    private static final ResourceLocation CONTENT_SYMBOL = sprite("content");
    private static final int SYMBOL_SIZE = 9;
    private static final int SYMBOL_WIDTH = 13;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_GAP = 2;
    private static final int COLUMN_GAP = 6;
    private static final int BAR_HEIGHT = 4;
    private static final int ROWS_TOP = 7;
    private static final int ROW_HEIGHT = 17;
    private static final int TEXT_OFFSET = 4;
    private static final int BAR_BACKGROUND = 0xFF14171C;
    private static final int BAR_WATER = 0xFF3F7BA6;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final int water;
    private final List<String> last;
    private final List<String> content;

    /**
     * Prepares the texts of a pan tooltip.
     *
     * @param tooltip the pan's state
     */
    public ClientPanTooltip(PanTooltip tooltip) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag(
                Minecraft.getInstance().getLanguageManager().getSelected().replace('_', '-')));
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        this.water = tooltip.water();
        this.last = tooltip.last().map(grains -> texts(grains, value -> I18n.get(LAST_KEY, format.format(value)))).orElse(List.of());
        this.content = texts(tooltip.content(),
                value -> I18n.get(CONTENT_KEY, format.format(value), Grains.PER_ITEM / 10));
    }

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "pan/" + name);
    }

    private static List<String> texts(Grains grains, DoubleFunction<String> text) {
        return List.of(text.apply(grains.pyrope() / 10.0), text.apply(grains.chromite() / 10.0),
                text.apply(grains.ilmenite() / 10.0));
    }

    private int columnWidth(Font font) {
        int widest = 0;
        for (String text : content) {
            widest = Math.max(widest, font.width(text));
        }
        return ICON_SIZE + TEXT_GAP + widest + COLUMN_GAP;
    }

    @Override
    public int getHeight() {
        return ROWS_TOP + 2 * ROW_HEIGHT + TEXT_GAP;
    }

    @Override
    public int getWidth(Font font) {
        return SYMBOL_WIDTH + MINERALS.size() * columnWidth(font) - COLUMN_GAP;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers) {
        drawRow(font, x, y + ROWS_TOP, matrix, buffers, last);
        drawRow(font, x, y + ROWS_TOP + ROW_HEIGHT, matrix, buffers, content);
    }

    private void drawRow(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers,
            List<String> texts) {
        for (int i = 0; i < texts.size(); i++) {
            font.drawInBatch(texts.get(i), x + SYMBOL_WIDTH + i * columnWidth(font) + ICON_SIZE + TEXT_GAP,
                    y + TEXT_OFFSET, TEXT_COLOR, true, matrix, buffers, Font.DisplayMode.NORMAL, 0,
                    LightTexture.FULL_BRIGHT);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int width = getWidth(font);
        graphics.fill(x, y, x + width, y + BAR_HEIGHT, BAR_BACKGROUND);
        graphics.fill(x, y, x + width * water / PanItem.CAPACITY, y + BAR_HEIGHT, BAR_WATER);
        drawIcons(font, x, y + ROWS_TOP, graphics, LAST_SYMBOL, !last.isEmpty());
        drawIcons(font, x, y + ROWS_TOP + ROW_HEIGHT, graphics, CONTENT_SYMBOL, true);
    }

    private void drawIcons(Font font, int x, int y, GuiGraphics graphics, ResourceLocation symbol, boolean minerals) {
        graphics.blitSprite(symbol, x, y + (ICON_SIZE - SYMBOL_SIZE) / 2, SYMBOL_SIZE, SYMBOL_SIZE);
        for (int i = 0; minerals && i < MINERALS.size(); i++) {
            graphics.renderFakeItem(new ItemStack(MINERALS.get(i).get()), x + SYMBOL_WIDTH + i * columnWidth(font), y);
        }
    }
}
