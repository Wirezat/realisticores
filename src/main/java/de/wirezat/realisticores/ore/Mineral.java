package de.wirezat.realisticores.ore;

/**
 * Presence of an ore inside a deposit.
 *
 * @param ore id of the ore
 * @param zone name of the rock zone of the deposit the ore replaces blocks in
 * @param fraction share of the blocks in that zone that become ore, between 0 and 1
 */
public record Mineral(String ore, String zone, double fraction) {
}
