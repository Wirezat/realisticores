package de.wirezat.realisticores.kimberlite;

import java.util.List;

/**
 * A cluster of kimberlite pipes.
 *
 * @param centerX field centre X in blocks
 * @param centerZ field centre Z in blocks
 * @param radius field radius in blocks
 * @param pipes pipes of the field
 */
public record Field(int centerX, int centerZ, int radius, List<Pipe> pipes) {
}
