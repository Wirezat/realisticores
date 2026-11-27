package de.wirezat.realisticores.kimberlite;

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

/** Indicator grains of kimberlite pipes, washed downhill along the trail of each pipe. */
public final class KimberliteIndicators implements IndicatorSource {
    public static final KimberliteIndicators INSTANCE = new KimberliteIndicators();

    private KimberliteIndicators() {
    }

    @Override
    public CompletableFuture<Measurement> measure(ServerLevel level, TerrainProbe probe, BlockPos pos) {
        PlumeParams params = Kimberlite.params().plume();
        PipeGrade grade = Kimberlite.grade();
        return Kimberlite.plumesReaching(level.getSeed(), probe, level.getSeaLevel(), pos.getX(), pos.getZ(),
                pos.getX(), pos.getZ())
                .thenApply(paths -> combine(paths, params, grade, level.getSeed(), pos.getX(), pos.getZ()));
    }

    /**
     * Measures the strongest trail at a position.
     *
     * @param paths trails that may reach the position
     * @param params trail settings
     * @param grade diamond grade of the pipes
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @return grains of the trail with the most of the lead indicator there
     */
    static Measurement combine(List<PlumePath<Pipe>> paths, PlumeParams params, PipeGrade grade, long seed, int x,
            int z) {
        PlumePath<Pipe> source = Plume.strongest(paths, x, z, params);
        if (source == null) {
            return Measurement.NONE;
        }
        Grains grains = Grains.measure(Plume.densities(source, x, z, params), seed, x, z);
        return new Measurement(grains, grains.isEmpty() ? null : source,
                GradeTone.of(grade.pipeFactor(source.source()), grains.get(params.lead().id())));
    }
}
