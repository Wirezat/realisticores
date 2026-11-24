package de.wirezat.realisticores.indicator;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * What the pan shows on hover.
 *
 * @param water water in the pan in millibuckets
 * @param last grains of the last wash, empty while it is computed or before the first one
 * @param content grains collected in the pan
 */
public record PanTooltip(int water, Optional<Grains> last, Grains content) implements TooltipComponent {
}
