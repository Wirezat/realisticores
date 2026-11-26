package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.ore.ActiveMineral;
import java.util.Arrays;
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
     * What the coal basin places, derived from its active minerals.
     *
     * @param blocks block id by rank, the ore or its replacement; ranks without either keep the rock of their layer
     * @param coal ranks whose coal is switched on; the others place their replacement
     * @param accessories minerals that accompany the coal, with the block and share of each
     */
    public record Seams(Map<Rank, String> blocks, Set<Rank> coal, List<ActiveMineral> accessories) {
        /**
         * Derives the seams from the active minerals of the coal basin.
         *
         * @param active minerals of the coal basin, enabled or replaced
         * @return what the basin places
         */
        public static Seams of(List<ActiveMineral> active) {
            return new Seams(CoalOres.blocks(active), coalRanks(active), CoalOres.accessories(active));
        }
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
            mineral.zones().stream().filter(CoalOres::isRank)
                    .forEach(zone -> blocks.put(Rank.valueOf(zone), mineral.block()));
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
                .filter(CoalOres::isRank).forEach(zone -> ranks.add(Rank.valueOf(zone)));
        return ranks;
    }

    /**
     * Lists the minerals that accompany the coal instead of forming a seam.
     *
     * @param active minerals of the coal basin, enabled or replaced
     * @return minerals none of whose zones is a coal rank
     */
    public static List<ActiveMineral> accessories(List<ActiveMineral> active) {
        return active.stream().filter(mineral -> isAccessory(mineral.zones())).toList();
    }

    /**
     * Tells whether a mineral accompanies the coal instead of forming a seam.
     *
     * @param zones rock zones of the mineral
     * @return true if none of the zones is a coal rank
     */
    public static boolean isAccessory(List<String> zones) {
        return zones.stream().noneMatch(CoalOres::isRank);
    }

    /**
     * Tells whether a zone is a coal rank.
     *
     * @param zone zone name
     * @return true for HARD_COAL, LIGNITE and ANTHRACITE
     */
    private static boolean isRank(String zone) {
        return Arrays.stream(Rank.values()).anyMatch(rank -> rank.name().equals(zone));
    }
}
