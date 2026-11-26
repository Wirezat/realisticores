package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.TerrainProbe;

/**
 * Terrain facts around one crater.
 *
 * @param meanRimY mean terrain height on the ring around the pipe
 * @param lowestRimY lowest terrain height on the ring
 * @param wet true if the biome at the crater has precipitation
 */
public record CraterSite(int meanRimY, int lowestRimY, boolean wet) {
    /**
     * Samples the terrain on a ring around a pipe.
     *
     * @param pipe the pipe
     * @param probe terrain reader
     * @param ringPoints number of terrain samples on the ring
     * @return the site facts
     */
    public static CraterSite measure(Pipe pipe, TerrainProbe probe, int ringPoints) {
        double radius = pipe.diameter() / 2.0;
        long sum = 0;
        int lowest = Integer.MAX_VALUE;
        for (int i = 0; i < ringPoints; i++) {
            double angle = 2.0 * Math.PI * i / ringPoints;
            int height = probe.terrainHeight(pipe.x() + (int) Math.round(Math.cos(angle) * radius),
                    pipe.z() + (int) Math.round(Math.sin(angle) * radius));
            sum += height;
            lowest = Math.min(lowest, height);
        }
        int mean = (int) Math.round(sum / (double) ringPoints);
        return new CraterSite(mean, lowest, probe.hasPrecipitation(pipe.x(), mean, pipe.z()));
    }
}
