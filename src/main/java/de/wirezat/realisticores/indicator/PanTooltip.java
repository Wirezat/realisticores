package de.wirezat.realisticores.indicator;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * What the pan shows on hover.
 *
 * @param water filling of the pan in percent
 * @param last grains of the last wash, empty while it is computed or before the first one
 * @param content grains collected in the pan
 * @param textWidth width in pixels of the widest text line around the picture, 0 if unknown
 */
public record PanTooltip(int water, Optional<Grains> last, Grains content, int textWidth)
        implements TooltipComponent {
}
