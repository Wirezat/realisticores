package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.kimberlite.Plume;
import de.wirezat.realisticores.kimberlite.PlumePath;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.jetbrains.annotations.Nullable;

/** Measures which indicator grains the sediment holds at a position. */
public final class PanYield {
    private PanYield() {
    }

    /**
     * Grains measured at one position.
     *
     * @param grains the measured grains
     * @param source trail the grains belong to, or null without grains
     */
    public record Measurement(Grains grains, @Nullable PlumePath source) {
    }

    /**
     * Measures a position in the background; trails not traced yet can take seconds.
     *
     * @param level the world
     * @param pos the sediment block being panned
     * @return future of the measurement
     */
    public static CompletableFuture<Measurement> measure(ServerLevel level, BlockPos pos) {
        if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return CompletableFuture.completedFuture(new Measurement(Grains.NONE, null));
        }
        KimberliteParams.PlumeParams params = Kimberlite.params().plume();
        return Kimberlite.plumesReaching(level.getSeed(), new LevelTerrainProbe(level, generator),
                level.getSeaLevel(), pos.getX(), pos.getZ(), pos.getX(), pos.getZ())
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
     * @return grains of the trail with the most pyrope there
     */
    static Measurement combine(List<PlumePath> paths, KimberliteParams.PlumeParams params, long seed, int x, int z) {
        double strongest = 0.0;
        PlumePath source = null;
        for (PlumePath path : paths) {
            double pyrope = Plume.density(path, x, z, params, params.pyrope());
            if (pyrope > strongest) {
                strongest = pyrope;
                source = path;
            }
        }
        if (source == null) {
            return new Measurement(Grains.NONE, null);
        }
        Grains grains = Grains.measure(strongest, Plume.density(source, x, z, params, params.chromite()),
                Plume.density(source, x, z, params, params.ilmenite()), seed, x, z);
        return new Measurement(grains, grains.isEmpty() ? null : source);
    }
}
