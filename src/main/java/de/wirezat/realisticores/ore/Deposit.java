package de.wirezat.realisticores.ore;

import java.util.List;

/**
 * A kind of ore deposit, e.g. the kimberlite pipe.
 *
 * @param id deposit id
 * @param minerals ores the deposit can contain
 */
public record Deposit(String id, List<Mineral> minerals) {
}
