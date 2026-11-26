package de.wirezat.realisticores.ore;

import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * One subcommand of {@code /locate realisticores}.
 *
 * @param name subcommand name; its title is the translation key {@code command.realisticores.locate.<name>}
 * @param search finds the nearest deposit of this kind
 */
public record LocateTarget(String name, Search search) {
    /** Finds the nearest deposit of one kind in an overworld whose deposit is active. */
    @FunctionalInterface
    public interface Search {
        /**
         * Runs the search on a background thread.
         *
         * @param level the world
         * @param probe reader for the original terrain
         * @param origin position of the command source
         * @param limit largest search radius in blocks
         * @return centre of the deposit, Y has no meaning; empty if none lies within the limit
         */
        Optional<BlockPos> nearest(ServerLevel level, TerrainProbe probe, BlockPos origin, int limit);
    }
}
