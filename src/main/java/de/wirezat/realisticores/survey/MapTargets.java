package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.indicator.Plume;
import de.wirezat.realisticores.kimberlite.Field;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberlitePipe;
import de.wirezat.realisticores.kimberlite.Pipe;
import de.wirezat.realisticores.porphyry.Porphyry;
import de.wirezat.realisticores.porphyry.PorphyryCopper;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** The trail points the survey maps lead to, one finder per deposit kind. */
public final class MapTargets {
    private static final int MAX_SEARCH = 40000;

    private MapTargets() {
    }

    /**
     * Finds a trail point of the nearest kimberlite field, preferring trails of pipes with diamonds.
     *
     * @param level the overworld
     * @param probe reader for the original terrain
     * @param player the player who uses the map
     * @return future of the point, empty if kimberlite is off or no field lies within reach
     */
    public static CompletableFuture<Optional<Plume.Spot>> kimberlite(ServerLevel level, TerrainProbe probe,
            Player player) {
        Optional<Field> field = OreConfig.resolved().active(KimberlitePipe.ID)
                ? Kimberlite.generator().nearestField(level.getSeed(), player.getBlockX(), player.getBlockZ(),
                        MAX_SEARCH)
                : Optional.empty();
        if (field.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        List<Pipe> pipes = new ArrayList<>(field.get().pipes());
        Util.shuffle(pipes, level.getRandom());
        return Kimberlite.mapSpot(level.getSeed(), probe, level.getSeaLevel(), pipes, level.getRandom().nextInt());
    }

    /**
     * Finds a trail point of the nearest porphyry copper deposit, searching for it in the background.
     *
     * @param level the overworld
     * @param probe reader for the original terrain
     * @param player the player who uses the map
     * @return future of the point, empty if porphyry copper is off or no deposit lies within reach
     */
    public static CompletableFuture<Optional<Plume.Spot>> porphyry(ServerLevel level, TerrainProbe probe,
            Player player) {
        if (!OreConfig.resolved().active(PorphyryCopper.ID)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        long seed = level.getSeed();
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int pick = level.getRandom().nextInt();
        return CompletableFuture.supplyAsync(() -> Porphyry.generator().nearest(seed, probe, x, z, MAX_SEARCH),
                        Util.backgroundExecutor())
                .thenCompose(body -> body.map(found -> Porphyry.mapSpot(seed, probe, level.getSeaLevel(),
                        List.of(found), pick)).orElse(CompletableFuture.completedFuture(Optional.empty())));
    }
}
