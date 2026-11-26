package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.Indicator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** All indicator minerals of all deposits, in the order of the deposit data. */
public final class Indicators {
    private static final List<Indicator> ALL = OreConfig.registry().deposits().values().stream()
            .flatMap(deposit -> deposit.indicators().stream()).toList();
    private static final Set<Indicator> LEADS = OreConfig.registry().deposits().values().stream()
            .filter(deposit -> !deposit.indicators().isEmpty()).map(deposit -> deposit.indicators().get(0))
            .collect(Collectors.toUnmodifiableSet());

    private Indicators() {
    }

    /**
     * Lists the indicator minerals.
     *
     * @return every indicator of every deposit
     */
    public static List<Indicator> all() {
        return ALL;
    }

    /**
     * Tells whether an indicator rates the trails of its deposit and shows the grade tone.
     *
     * @param indicator the indicator
     * @return true for the first indicator of a deposit
     */
    public static boolean isLead(Indicator indicator) {
        return LEADS.contains(indicator);
    }

    /**
     * Gives the item one full portion of an indicator's grains turns into.
     *
     * @param indicator the indicator
     * @return the item, air if the id names no item
     */
    public static Item item(Indicator indicator) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(indicator.item()));
    }
}
