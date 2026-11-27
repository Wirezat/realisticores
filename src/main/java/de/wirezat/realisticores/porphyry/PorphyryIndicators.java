package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.indicator.GradeTone;
import de.wirezat.realisticores.indicator.Grains;
import de.wirezat.realisticores.indicator.IndicatorSource;
import de.wirezat.realisticores.indicator.Measurement;
import de.wirezat.realisticores.indicator.Plume;
import de.wirezat.realisticores.indicator.PlumeParams;
import de.wirezat.realisticores.indicator.PlumePath;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Indicator grains of porphyry copper deposits, washed downhill from the weathered top of each deposit. */
public final class PorphyryIndicators implements IndicatorSource {
    public static final PorphyryIndicators INSTANCE = new PorphyryIndicators();

    private PorphyryIndicators() {
    }

    @Override
    public CompletableFuture<Measurement> measure(ServerLevel level, TerrainProbe probe, BlockPos pos) {
        PlumeParams params = Porphyry.params().plume();
        return Porphyry.plumesReaching(level.getSeed(), probe, level.getSeaLevel(), pos.getX(), pos.getZ(),
                pos.getX(), pos.getZ())
                .thenApply(paths -> combine(paths, params, level.getSeed(), pos.getX(), pos.getZ()));
    }

    /**
     * Measures the strongest trail at a position.
     *
     * @param paths trails that may reach the position
     * @param params trail settings
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @return grains of the trail with the most of the lead indicator there, shown without a grade tone
     */
    static Measurement combine(List<PlumePath<Body>> paths, PlumeParams params, long seed, int x, int z) {
        PlumePath<Body> source = Plume.strongest(paths, x, z, params);
        if (source == null) {
            return Measurement.NONE;
        }
        Grains grains = Grains.measure(Plume.densities(source, x, z, params), seed, x, z);
        return new Measurement(grains, grains.isEmpty() ? null : source, GradeTone.RED);
    }
}
