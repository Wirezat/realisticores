package de.wirezat.realisticores.ore;

/**
 * One ore this mod can place.
 *
 * @param id ore id used in the config, e.g. {@code peridot}
 * @param block id of the block placed for this ore unless the config overrides it
 * @param tag id of the block tag that marks blocks of this ore, e.g. {@code c:ores/peridot}
 * @param xpMin least experience dropped when the ore block is mined
 * @param xpMax most experience dropped when the ore block is mined
 */
public record OreDefinition(String id, String block, String tag, int xpMin, int xpMax) {
}
