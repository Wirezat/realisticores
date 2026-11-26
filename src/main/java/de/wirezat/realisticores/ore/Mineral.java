package de.wirezat.realisticores.ore;

import java.util.List;
import java.util.Map;

/**
 * Presence of an ore inside a deposit.
 *
 * @param ore id of the ore
 * @param zones names of the rock zones of the deposit the ore replaces blocks in
 * @param fraction share of the blocks in those zones that become ore, between 0 and 1
 * @param graded true if the share is scaled by the grade of the individual pipe and rock phase
 * @param zoneBlocks block ids by zone that replace the ore's block in that zone
 */
public record Mineral(String ore, List<String> zones, double fraction, boolean graded, Map<String, String> zoneBlocks) {
}
