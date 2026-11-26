package de.wirezat.realisticores.ore;

/**
 * An indicator mineral: grains a deposit sheds into the sediment around it, found by panning.
 *
 * @param id indicator id, also the config key prefix and, for a lead indicator, the sprite prefix of its tone
 * @param item id of the item one full portion of grains turns into
 * @param length default trail length in blocks over which the grains fall to 1/e
 * @param peak default density next to the deposit, 0 to 1; 1 is 100 g per wash
 * @param pitch pitch of the chime played when a wash holds this mineral
 */
public record Indicator(String id, String item, double length, double peak, float pitch) {
}
