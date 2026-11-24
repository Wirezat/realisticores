package de.wirezat.realisticores.ore;

/**
 * A mineral that is switched on, with the block to place.
 *
 * @param ore id of the ore
 * @param zone name of the rock zone the ore replaces blocks in
 * @param fraction share of the blocks in that zone that become ore
 * @param block id of the block to place
 */
public record ActiveMineral(String ore, String zone, double fraction, String block) {
}
