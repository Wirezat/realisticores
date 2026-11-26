package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.Deposits;
import de.wirezat.realisticores.worldgen.LevelTerrainProbe;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Measures which indicator grains the sediment holds at a position. */
public final class PanYield {
    private PanYield() {
    }

    /**
     * Measures a position in the background; trails not traced yet can take seconds.
     *
     * @param level the world
     * @param pos the sediment block being panned
     * @return future of the strongest measurement of all active deposits that shed indicator grains
     */
    public static CompletableFuture<Measurement> measure(ServerLevel level, BlockPos pos) {
        Optional<TerrainProbe> probe = LevelTerrainProbe.of(level);
        if (level.dimension() != Level.OVERWORLD || probe.isEmpty()) {
            return CompletableFuture.completedFuture(Measurement.NONE);
        }
        OreConfig.Resolved config = OreConfig.resolved();
        List<CompletableFuture<Measurement>> measurements = Deposits.ALL.stream()
                .filter(deposit -> config.active(deposit.id()))
                .map(DepositType::indicators).flatMap(Optional::stream)
                .map(source -> source.measure(level, probe.get(), pos)).toList();
        return CompletableFuture.allOf(measurements.toArray(CompletableFuture[]::new))
                .thenApply(done -> strongest(measurements.stream().map(CompletableFuture::join).toList()));
    }

    /**
     * Picks the measurement with the most grains.
     *
     * @param measurements one measurement per deposit kind
     * @return the strongest one, {@link Measurement#NONE} without any
     */
    static Measurement strongest(List<Measurement> measurements) {
        return measurements.stream().max(Comparator.comparingInt(measurement -> measurement.grains().total()))
                .orElse(Measurement.NONE);
    }
}
