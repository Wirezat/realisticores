package de.wirezat.realisticores.client;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.indicator.Grains;
import de.wirezat.realisticores.indicator.PanTooltip;
import de.wirezat.realisticores.indicator.PyropeTone;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
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

/** Draws the grains of a pan as two rows of icons and numbers, and its water as a bar across the tooltip. */
public final class ClientPanTooltip implements ClientTooltipComponent {
    private static final List<DeferredItem<Item>> MINERALS =
            List.of(ModItems.PYROPE, ModItems.CHROMITE, ModItems.ILMENITE);
    private static final String GRAMS_KEY = "tooltip.realisticores.pan.grams";
    private static final String WATER_KEY = "tooltip.realisticores.pan.water";
    private static final ResourceLocation LAST_SYMBOL = sprite("measurement");
    private static final ResourceLocation CONTENT_SYMBOL = sprite("content");
    private static final int SYMBOL_SIZE = 9;
    private static final int SYMBOL_WIDTH = 13;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_GAP = 2;
    private static final int COLUMN_GAP = 6;
    private static final int ROW_HEIGHT = 17;
    private static final int TEXT_OFFSET = 4;
    private static final int WATER_TOP = 2 * ROW_HEIGHT + 1;
    private static final int BAR_TOP = WATER_TOP + 10;
    private static final int BAR_HEIGHT = 7;
    private static final int STROKE_SPACING = 2;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int WATER_COLOR = 0xFF3264FF;
    private static final int EMPTY_COLOR = 0xFF6B6B6B;

    private final int water;
    private final int textWidth;
    private final PyropeTone tone;
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
        this.textWidth = tooltip.textWidth();
        this.tone = tooltip.tone();
        this.last = tooltip.last().map(grains -> texts(grains, format)).orElse(List.of());
        this.content = texts(tooltip.content(), format);
    }

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "pan/" + name);
    }

    private static List<String> texts(Grains grains, NumberFormat format) {
        return Stream.of(grains.pyrope(), grains.chromite(), grains.ilmenite())
                .map(tenths -> I18n.get(GRAMS_KEY, format.format(tenths / 10.0))).toList();
    }

    private int columnWidth(Font font) {
        int widest = Stream.concat(last.stream(), content.stream()).mapToInt(font::width).max().orElse(0);
        return ICON_SIZE + TEXT_GAP + widest + COLUMN_GAP;
    }

    @Override
    public int getHeight() {
        return BAR_TOP + BAR_HEIGHT + TEXT_GAP;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(textWidth, SYMBOL_WIDTH + MINERALS.size() * columnWidth(font) - COLUMN_GAP);
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers) {
        drawRow(font, x, y, matrix, buffers, last);
        drawRow(font, x, y + ROW_HEIGHT, matrix, buffers, content);
        drawText(font, I18n.get(WATER_KEY, water), x, y + WATER_TOP, WATER_COLOR, matrix, buffers);
    }

    private void drawRow(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffers,
            List<String> texts) {
        for (int i = 0; i < texts.size(); i++) {
            drawText(font, texts.get(i), x + SYMBOL_WIDTH + i * columnWidth(font) + ICON_SIZE + TEXT_GAP,
                    y + TEXT_OFFSET, TEXT_COLOR, matrix, buffers);
        }
    }

    private static void drawText(Font font, String text, int x, int y, int color, Matrix4f matrix,
            MultiBufferSource.BufferSource buffers) {
        font.drawInBatch(text, x, y, color, true, matrix, buffers, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        drawIcons(font, x, y, graphics, LAST_SYMBOL, last.isEmpty() ? List.of() : MINERALS, tone);
        drawIcons(font, x, y + ROW_HEIGHT, graphics, CONTENT_SYMBOL, MINERALS, PyropeTone.RED);
        int strokes = getWidth(font) / STROKE_SPACING;
        for (int i = 0; i < strokes; i++) {
            int left = x + i * STROKE_SPACING;
            graphics.fill(left, y + BAR_TOP, left + 1, y + BAR_TOP + BAR_HEIGHT,
                    i * 100 < water * strokes ? WATER_COLOR : EMPTY_COLOR);
        }
    }

    private void drawIcons(Font font, int x, int y, GuiGraphics graphics, ResourceLocation symbol,
            List<DeferredItem<Item>> minerals, PyropeTone pyropeTone) {
        graphics.blitSprite(symbol, x, y + (ICON_SIZE - SYMBOL_SIZE) / 2, SYMBOL_SIZE, SYMBOL_SIZE);
        for (int i = 0; i < minerals.size(); i++) {
            int left = x + SYMBOL_WIDTH + i * columnWidth(font);
            if (minerals.get(i) == ModItems.PYROPE && pyropeTone != PyropeTone.RED) {
                graphics.blitSprite(sprite("pyrope_" + pyropeTone.getSerializedName()), left, y, ICON_SIZE, ICON_SIZE);
            } else {
                graphics.renderFakeItem(new ItemStack(minerals.get(i).get()), left, y);
            }
        }
    }
}
