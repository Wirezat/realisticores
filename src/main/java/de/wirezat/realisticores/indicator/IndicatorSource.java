package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** A deposit that sheds indicator grains into the sediment around it. */
@FunctionalInterface
public interface IndicatorSource {
    /**
     * Measures the grains of this deposit kind at a position, possibly in the background.
     *
     * @param level the world, an active overworld
     * @param probe reader for the original terrain
     * @param pos the sediment block being panned
     * @return future of the measurement, {@link Measurement#NONE} without grains
     */
    CompletableFuture<Measurement> measure(ServerLevel level, TerrainProbe probe, BlockPos pos);
}
