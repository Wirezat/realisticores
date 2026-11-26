package de.wirezat.realisticores.ore;

import java.util.List;

/**
 * A kind of ore deposit, e.g. the kimberlite pipe.
 *
 * @param id deposit id
 * @param minerals ores the deposit can contain
 * @param indicators indicator minerals the deposit sheds; the first is the lead indicator, which rates trails and shows
 *     the grade tone; empty for none
 */
public record Deposit(String id, List<Mineral> minerals, List<Indicator> indicators) {
}
