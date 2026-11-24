package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.indicator.LevelTerrainProbe;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.kimberlite.Pipe;
import de.wirezat.realisticores.kimberlite.Plume;
import de.wirezat.realisticores.kimberlite.PlumePath;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Paints the indicator trails of the pipes in a map's area onto the map. */
final class SurveyOverlay {
    private static final int MAP_SIZE = 128;
    private static final double MIN_CHANCE = 0.01;
    private static final double LOW_CHANCE = 0.04;
    private static final double MEDIUM_CHANCE = 0.12;
    private static final double HIGH_CHANCE = 0.4;
    private static final double WIDTH_SIGMAS = 3.0;
    private static final double LINE_HALF_WIDTH_PIXELS = 0.5;

    private SurveyOverlay() {
    }

    private record Trail(PlumePath path, double minX, double maxX, double minZ, double maxZ) {
    }

    /**
     * Computes the trails of all pipes whose grains reach the map area on a background thread and then paints
     * them onto the map on the server thread; brighter means a higher find chance.
     *
     * @param level the world
     * @param mapId id of the map to paint on
     */
    static void paintAsync(ServerLevel level, MapId mapId) {
        MapItemSavedData data = level.getMapData(mapId);
        if (data == null || level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return;
        }
        KimberliteParams.PlumeParams params = Kimberlite.params().plume();
        int blocksPerPixel = 1 << data.scale;
        int west = data.centerX - MAP_SIZE / 2 * blocksPerPixel;
        int north = data.centerZ - MAP_SIZE / 2 * blocksPerPixel;
        double halfWidth = blocksPerPixel * LINE_HALF_WIDTH_PIXELS;
        CompletableFuture.supplyAsync(
                () -> trailsReaching(level, generator, params, west, north, MAP_SIZE * blocksPerPixel, halfWidth),
                Util.backgroundExecutor())
                .thenAcceptAsync(trails -> paint(level, mapId, trails, params, west, north, blocksPerPixel, halfWidth),
                        level.getServer());
    }

    private static void paint(ServerLevel level, MapId mapId, List<Trail> trails, KimberliteParams.PlumeParams params,
            int west, int north, int blocksPerPixel, double halfWidth) {
        MapItemSavedData data = level.getMapData(mapId);
        if (data == null) {
            return;
        }
        for (int py = 0; py < MAP_SIZE; py++) {
            for (int px = 0; px < MAP_SIZE; px++) {
                double x = west + (px + 0.5) * blocksPerPixel;
                double z = north + (py + 0.5) * blocksPerPixel;
                double strongest = 0.0;
                for (Trail trail : trails) {
                    if (x >= trail.minX() && x <= trail.maxX() && z >= trail.minZ() && z <= trail.maxZ()) {
                        strongest = Math.max(strongest,
                                Plume.lineDensity(trail.path(), x, z, params, params.pyrope(), halfWidth));
                    }
                }
                if (strongest >= MIN_CHANCE) {
                    data.setColor(px, py, color(strongest));
                }
            }
        }
    }

    private static List<Trail> trailsReaching(ServerLevel level, NoiseBasedChunkGenerator generator,
            KimberliteParams.PlumeParams params, int west, int north, int size, double halfWidth) {
        LevelTerrainProbe probe = new LevelTerrainProbe(level, generator);
        int reach = (int) Math.ceil(params.maxLength() * Plume.maxFactor(params));
        return Kimberlite.generator().pipesNear(level.getSeed(), west - reach, north - reach, west + size + reach,
                north + size + reach).parallelStream()
                .filter(pipe -> distanceToArea(pipe, west, north, size)
                        <= params.maxLength() * Kimberlite.plumeFactor(level.getSeed(), pipe, probe) + pipe.diameter())
                .map(pipe -> boxOf(Kimberlite.plume(level.getSeed(), pipe, probe, level.getSeaLevel()),
                        WIDTH_SIGMAS * halfWidth + pipe.diameter() / 2.0))
                .toList();
    }

    private static double distanceToArea(Pipe pipe, int west, int north, int size) {
        double dx = Math.max(0, Math.max(west - pipe.x(), pipe.x() - (west + size)));
        double dz = Math.max(0, Math.max(north - pipe.z(), pipe.z() - (north + size)));
        return Math.hypot(dx, dz);
    }

    private static Trail boxOf(PlumePath path, double margin) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < path.xs().length; i++) {
            minX = Math.min(minX, path.xs()[i]);
            maxX = Math.max(maxX, path.xs()[i]);
            minZ = Math.min(minZ, path.zs()[i]);
            maxZ = Math.max(maxZ, path.zs()[i]);
        }
        return new Trail(path, minX - margin, maxX + margin, minZ - margin, maxZ + margin);
    }

    private static byte color(double chance) {
        MapColor.Brightness brightness = chance >= HIGH_CHANCE ? MapColor.Brightness.HIGH
                : chance >= MEDIUM_CHANCE ? MapColor.Brightness.NORMAL
                : chance >= LOW_CHANCE ? MapColor.Brightness.LOW : MapColor.Brightness.LOWEST;
        return MapColor.DIAMOND.getPackedId(brightness);
    }
}
