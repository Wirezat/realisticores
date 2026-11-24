package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.indicator.LevelTerrainProbe;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberliteParams;
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
    static final int MAP_SIZE = 128;
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
     * Computes the water outlines of a map and the trails of all pipes whose grains reach it in the background and
     * paints them on the server thread, the outlines first; brighter trails mean a higher find chance.
     *
     * @param level the world
     * @param mapId id of the map to paint on
     */
    static void paintAsync(ServerLevel level, MapId mapId) {
        MapItemSavedData data = level.getMapData(mapId);
        if (data == null) {
            return;
        }
        int blocksPerPixel = 1 << data.scale;
        int west = data.centerX - MAP_SIZE / 2 * blocksPerPixel;
        int north = data.centerZ - MAP_SIZE / 2 * blocksPerPixel;
        CompletableFuture<Void> outlines = CompletableFuture
                .supplyAsync(() -> BiomePreview.colors(level, west, north, blocksPerPixel), Util.backgroundExecutor())
                .thenAcceptAsync(colors -> paint(level, mapId, colors), level.getServer());
        outlines.whenComplete(Kimberlite::logFailure);
        if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return;
        }
        KimberliteParams.PlumeParams params = Kimberlite.params().plume();
        double halfWidth = blocksPerPixel * LINE_HALF_WIDTH_PIXELS;
        Kimberlite.plumesReaching(level.getSeed(), new LevelTerrainProbe(level, generator), level.getSeaLevel(),
                west, north, west + MAP_SIZE * blocksPerPixel, north + MAP_SIZE * blocksPerPixel)
                .thenApply(paths -> colors(paths, params, west, north, blocksPerPixel, halfWidth))
                .thenAcceptBothAsync(outlines.exceptionally(failure -> null),
                        (colors, painted) -> paint(level, mapId, colors), level.getServer())
                .whenComplete(Kimberlite::logFailure);
    }

    private static byte[] colors(List<PlumePath> paths, KimberliteParams.PlumeParams params, int west, int north,
            int blocksPerPixel, double halfWidth) {
        List<Trail> trails = paths.stream()
                .map(path -> boxOf(path, WIDTH_SIGMAS * halfWidth + path.pipe().diameter() / 2.0)).toList();
        byte[] colors = new byte[MAP_SIZE * MAP_SIZE];
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
                    colors[py * MAP_SIZE + px] = color(strongest);
                }
            }
        }
        return colors;
    }

    private static void paint(ServerLevel level, MapId mapId, byte[] colors) {
        MapItemSavedData data = level.getMapData(mapId);
        if (data == null) {
            return;
        }
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != 0) {
                data.setColor(i % MAP_SIZE, i / MAP_SIZE, colors[i]);
            }
        }
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
