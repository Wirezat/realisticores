package de.wirezat.realisticores.ore;

import java.util.List;
import java.util.Map;

/**
 * A mineral of an active deposit with the block to place: its ore when switched on, its replacement otherwise.
 *
 * @param ore id of the ore
 * @param zones names of the rock zones the ore replaces blocks in
 * @param fraction share of the blocks in those zones that become ore
 * @param graded true if the share is scaled by the grade of the individual pipe and rock phase
 * @param block id of the block to place
 * @param enabled true if the ore is switched on, false if the block is its replacement
 * @param zoneBlocks block ids by zone that replace {@code block} in that zone
 */
public record ActiveMineral(String ore, List<String> zones, double fraction, boolean graded, String block,
        boolean enabled, Map<String, String> zoneBlocks) {
    /**
     * Creates a mineral that places one block in all its zones.
     *
     * @param ore id of the ore
     * @param zones names of the rock zones the ore replaces blocks in
     * @param fraction share of the blocks in those zones that become ore
     * @param graded true if the share is scaled by the grade of the individual pipe and rock phase
     * @param block id of the block to place
     * @param enabled true if the ore is switched on, false if the block is its replacement
     */
    public ActiveMineral(String ore, List<String> zones, double fraction, boolean graded, String block,
            boolean enabled) {
        this(ore, zones, fraction, graded, block, enabled, Map.of());
    }

    /**
     * Gives the block placed in one zone.
     *
     * @param zone name of the zone
     * @return block id for that zone
     */
    public String blockFor(String zone) {
        return zoneBlocks.getOrDefault(zone, block);
    }
}
