package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Arrays;

/**
 * Terrain facts of one basin.
 *
 * @param datum median terrain height of the inner part
 * @param relief difference between the highest and the lowest inner point
 * @param highRimPoints points on the rim that stand well above the datum
 */
public record BasinSite(int datum, int relief, int highRimPoints) {
    private static final int RING_POINTS = 8;

    /**
     * Samples the terrain at the centre, on the half ellipse and on the rim.
     *
     * @param basin the basin
     * @param probe terrain reader
     * @param params relief settings
     * @return the site facts
     */
    public static BasinSite measure(Basin basin, TerrainProbe probe, CoalParams.ReliefParams params) {
        int[] inner = new int[RING_POINTS + 1];
        int[] rim = new int[RING_POINTS];
        inner[RING_POINTS] = probe.terrainHeight(basin.x(), basin.z());
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = 2.0 * Math.PI * i / RING_POINTS;
            double along = Math.cos(angle) * basin.longRadius();
            double across = Math.sin(angle) * basin.shortRadius();
            double dx = along * Math.cos(basin.strike()) - across * Math.sin(basin.strike());
            double dz = along * Math.sin(basin.strike()) + across * Math.cos(basin.strike());
            inner[i] = probe.terrainHeight(basin.x() + (int) Math.round(dx / 2.0),
                    basin.z() + (int) Math.round(dz / 2.0));
            rim[i] = probe.terrainHeight(basin.x() + (int) Math.round(dx), basin.z() + (int) Math.round(dz));
        }
        Arrays.sort(inner);
        int datum = inner[inner.length / 2];
        int high = (int) Arrays.stream(rim).filter(y -> y >= datum + params.foothillRise()).count();
        return new BasinSite(datum, inner[inner.length - 1] - inner[0], high);
    }

    /**
     * Tells whether the basin would lie in high mountains.
     *
     * @param params relief settings
     * @return true if there is no basin here
     */
    public boolean mountain(CoalParams.ReliefParams params) {
        return relief > params.mountainRelief();
    }

    /**
     * Gives the type of the basin after the terrain is known.
     *
     * @param basin the basin
     * @param params relief settings
     * @return folded at the foot of mountains, else the rolled type
     */
    public BasinType type(Basin basin, CoalParams.ReliefParams params) {
        return highRimPoints >= params.foothillPoints() ? BasinType.FOLDED : basin.rolledType();
    }
}
