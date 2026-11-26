package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ore.ActiveMineral;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Decides which block each coal rank places. */
public final class CoalOres {
    private CoalOres() {
    }

    /**
     * Maps the ranks to the blocks of the enabled ores.
     *
     * @param active enabled minerals of the coal basin
     * @return block id by rank, the ore or its replacement; ranks without either are absent
     */
    public static Map<Rank, String> blocks(List<ActiveMineral> active) {
        Map<Rank, String> blocks = new EnumMap<>(Rank.class);
        for (ActiveMineral mineral : active) {
            mineral.zones().forEach(zone -> blocks.put(Rank.valueOf(zone), mineral.block()));
        }
        return blocks;
    }

    /**
     * Gives the ranks whose coal is switched on.
     *
     * @param active minerals of the coal basin
     * @return ranks placing their own ore rather than a replacement
     */
    public static Set<Rank> coalRanks(List<ActiveMineral> active) {
        Set<Rank> ranks = EnumSet.noneOf(Rank.class);
        active.stream().filter(ActiveMineral::enabled).flatMap(mineral -> mineral.zones().stream())
                .forEach(zone -> ranks.add(Rank.valueOf(zone)));
        return ranks;
    }

    /**
     * Tells whether a basin holds any coal.
     *
     * @param type type of the basin
     * @param ranks ranks that have a block
     * @return true if at least one rank of the basin has a block
     */
    public static boolean generates(BasinType type, Set<Rank> ranks) {
        return switch (type) {
            case HARD_COAL -> ranks.contains(Rank.HARD_COAL);
            case LIGNITE -> ranks.contains(Rank.LIGNITE);
            case FOLDED -> ranks.contains(Rank.HARD_COAL) || ranks.contains(Rank.ANTHRACITE);
        };
    }
}
