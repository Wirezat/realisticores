package de.wirezat.realisticores.ore;

import java.util.List;

/**
 * A mineral of an active deposit with the block to place: its ore when switched on, its replacement otherwise.
 *
 * @param ore id of the ore
 * @param zones names of the rock zones the ore replaces blocks in
 * @param fraction share of the blocks in those zones that become ore
 * @param graded true if the share is scaled by the grade of the individual pipe and rock phase
 * @param block id of the block to place
 * @param enabled true if the ore is switched on, false if the block is its replacement
 */
public record ActiveMineral(String ore, List<String> zones, double fraction, boolean graded, String block,
        boolean enabled) {
}
