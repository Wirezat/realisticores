package de.wirezat.realisticores.ore;

import java.util.List;

/**
 * A mineral that is switched on, with the block to place.
 *
 * @param ore id of the ore
 * @param zones names of the rock zones the ore replaces blocks in
 * @param fraction share of the blocks in those zones that become ore
 * @param graded true if the share is scaled by the grade of the individual pipe and rock phase
 * @param block id of the block to place
 */
public record ActiveMineral(String ore, List<String> zones, double fraction, boolean graded, String block) {
}
