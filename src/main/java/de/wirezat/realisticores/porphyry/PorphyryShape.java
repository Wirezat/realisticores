package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.worldgen.ValueNoise;

/** Shells of hydrothermal alteration around a porphyry stock and the weathering profile cut into them. */
public final class PorphyryShape {
    public static final double OUTER = 2.0;
    static final double CORE_EDGE = 0.4;
    static final double POTASSIC_EDGE = 1.0;
    static final double PHYLLIC_EDGE = 1.45;

    private static final double FRAY = 1.75;
    private static final double FRAY_WAVELENGTH = 5.0;
    private static final double CENTRE_DEPTH = 1.25;
    private static final double DOME = 0.8;
    private static final double WARP = 0.15;
    private static final double WARP_WAVELENGTH = 0.8;
    private static final double EDGE_NOISE = 0.05;
    private static final double EDGE_WAVELENGTH = 7.0;
    private static final int BOTTOM_Y = -36;
    private static final double BOTTOM_NOISE = 9.0;
    private static final double BOTTOM_WAVELENGTH = 30.0;
    private static final int SEA_Y = 63;
    private static final double WATER_SLOPE = 0.4;
    private static final double WATER_BASE = 8.0;
    private static final double MIN_WATER = 14.0;
    private static final double MAX_WATER = 34.0;
    private static final double LEACHED_MIN = 3.0;
    private static final double OXIDE_BASE = 4.0;
    private static final double GOSSAN_NOISE = 1.5;
    private static final double GOSSAN_WAVELENGTH = 17.0;
    private static final double CEMENT_NOISE = 2.0;
    private static final double CEMENT_WAVELENGTH = 23.0;

    /**
     * Gives the lowest Y any deposit reaches.
     *
     * @return bottom limit in blocks
     */
    public static int bottomLimit() {
        return (int) (BOTTOM_Y - BOTTOM_NOISE);
    }

    /**
     * Gives the largest horizontal distance from the axis at which a deposit can still change a block.
     *
     * @param body the deposit
     * @return reach in blocks
     */
    public static double reach(Body body) {
        return (OUTER + WARP + EDGE_NOISE) * body.elongation() * body.radius();
    }

    /**
     * Gives the height of the dome centre, where the shells turn from a dome into a vertical stem.
     *
     * @param body the deposit
     * @return Y of the dome centre
     */
    public double centreY(Body body) {
        return body.surfaceY() - body.stage().cover() - CENTRE_DEPTH * body.radius();
    }

    /**
     * Measures how far a block lies from the stock, in units of the ore shell radius.
     *
     * @param seed world seed
     * @param body the deposit
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return shell value: below 0.4 core, below 1 potassic, below 1.45 phyllic, below 2 propylitic
     */
    public double shell(long seed, Body body, int x, int y, int z) {
        long s = seed ^ body.salt();
        double dx = x - body.x();
        double dz = z - body.z();
        double cos = Math.cos(body.angle());
        double sin = Math.sin(body.angle());
        double wavelength = WARP_WAVELENGTH * body.radius();
        double warp = WARP * body.radius();
        double u = (dx * cos + dz * sin) / body.elongation() + warp * ValueNoise.sample3(s + 1, x, y, z, wavelength);
        double v = dz * cos - dx * sin + warp * ValueNoise.sample3(s + 2, x, y, z, wavelength);
        double r = Math.hypot(u, v);
        double yc = centreY(body);
        double d = y > yc ? Math.hypot(r, DOME * (y - yc)) : r;
        return d / body.radius() + EDGE_NOISE * ValueNoise.sample3(s + 3, x, y, z, EDGE_WAVELENGTH);
    }

    /**
     * Gives the rock zone of a block.
     *
     * @param seed world seed
     * @param body the deposit
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @param surfaceY terrain height of the column
     * @param smoothY smoothed terrain height of the column, which sets the water table
     * @return the zone, HOST outside the deposit
     */
    public Zone zone(long seed, Body body, int x, int y, int z, int surfaceY, double smoothY) {
        return zone(seed, body, shell(seed, body, x, y, z), x, y, z, surfaceY, smoothY);
    }

    /**
     * Gives the rock zone of a block whose shell value is known.
     *
     * @param seed world seed
     * @param body the deposit
     * @param q shell value of the block
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @param surfaceY terrain height of the column
     * @param smoothY smoothed terrain height of the column, which sets the water table
     * @return the zone, HOST outside the deposit
     */
    public Zone zone(long seed, Body body, double q, int x, int y, int z, int surfaceY, double smoothY) {
        long s = seed ^ body.salt();
        if (y < BOTTOM_Y + BOTTOM_NOISE * ValueNoise.sample2(s + 4, x, z, BOTTOM_WAVELENGTH)) {
            return Zone.HOST;
        }
        if (q > OUTER || q > FRAY
                && ValueNoise.sample3(s + 5, x, y, z, FRAY_WAVELENGTH) > (OUTER - q) / (OUTER - FRAY) * 2 - 1) {
            return Zone.HOST;
        }
        if (q < PHYLLIC_EDGE) {
            Zone weathered = weathered(s, body.stage(), q, x, z, surfaceY - y, smoothY);
            if (weathered != null) {
                return weathered;
            }
        }
        return q < CORE_EDGE ? Zone.CORE : q < POTASSIC_EDGE ? Zone.POTASSIC : q < PHYLLIC_EDGE ? Zone.PHYLLIC
                : Zone.PROPYLITIC;
    }

    /**
     * Gives the weathering zone of a block from its depth below the surface.
     *
     * @param s noise seed of the deposit
     * @param stage erosion stage
     * @param q shell value
     * @param x world X
     * @param z world Z
     * @param depth blocks below the surface
     * @param smoothY smoothed terrain height, which sets the water table
     * @return the weathering zone, null below the cementation zone
     */
    private static Zone weathered(long s, Stage stage, double q, int x, int z, int depth, double smoothY) {
        double water = Math.max(MIN_WATER, Math.min(MAX_WATER, WATER_SLOPE * (smoothY - SEA_Y) + WATER_BASE));
        double gossan = stage.gossan() + GOSSAN_NOISE * ValueNoise.sample2(s + 6, x, z, GOSSAN_WAVELENGTH);
        double oxide = Math.min(stage.oxide(), water - gossan - LEACHED_MIN);
        double cementation = stage.cementation() + CEMENT_NOISE * ValueNoise.sample2(s + 7, x, z, CEMENT_WAVELENGTH);
        if (depth < gossan && (stage != Stage.DEEP || q > CORE_EDGE)) {
            return Zone.GOSSAN;
        }
        if (depth < water - oxide) {
            return Zone.LEACHED;
        }
        if (depth < water) {
            return depth >= water - OXIDE_BASE ? Zone.OXIDE_BASE : Zone.OXIDE;
        }
        return depth < water + cementation ? Zone.CEMENTATION : null;
    }
}
