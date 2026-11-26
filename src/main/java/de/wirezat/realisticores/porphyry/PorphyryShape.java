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
    private static final double VENT_TOP = 10.0;
    private static final double VENT_WANDER = 2.0;
    private static final double VENT_WAVELENGTH = 9.0;

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
        return (OUTER + WARP + EDGE_NOISE) * body.elongation() * body.radius()
                + Math.hypot(body.tiltX(), body.tiltZ()) * (body.surfaceY() - bottomLimit());
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
        double yc = centreY(body);
        double dx = x - body.x() - body.tiltX() * (y - yc);
        double dz = z - body.z() - body.tiltZ() * (y - yc);
        double cos = Math.cos(body.angle());
        double sin = Math.sin(body.angle());
        double wavelength = WARP_WAVELENGTH * body.radius();
        double warp = WARP * body.radius();
        double u = (dx * cos + dz * sin) / body.elongation() + warp * ValueNoise.sample3(s + 1, x, y, z, wavelength);
        double v = dz * cos - dx * sin + warp * ValueNoise.sample3(s + 2, x, y, z, wavelength);
        double r = Math.hypot(u, v);
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
        if (q < PHYLLIC_EDGE && inVent(s, body, x, y, z)) {
            return Zone.BRECCIA;
        }
        if (inDike(body, x, y, z)) {
            return Zone.CORE;
        }
        return q < CORE_EDGE ? Zone.CORE : q < POTASSIC_EDGE ? Zone.POTASSIC : q < PHYLLIC_EDGE ? Zone.PHYLLIC
                : Zone.PROPYLITIC;
    }

    /**
     * Tells whether a block lies in one of the deposit's breccia vents, which end a little above the potassic dome.
     *
     * @param s noise seed of the deposit
     * @param body the deposit
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return true inside a vent
     */
    private boolean inVent(long s, Body body, int x, int y, int z) {
        double yc = centreY(body);
        if (y > yc + body.radius() / DOME + VENT_TOP) {
            return false;
        }
        double cos = Math.cos(body.angle());
        double sin = Math.sin(body.angle());
        for (int i = 0; i < body.vents().size(); i++) {
            Body.Vent vent = body.vents().get(i);
            double along = vent.u() * body.radius() * body.elongation();
            double across = vent.v() * body.radius();
            double vx = body.x() + body.tiltX() * (y - yc) + along * cos - across * sin
                    + VENT_WANDER * ValueNoise.sample2(s + 8 + i, y, 0, VENT_WAVELENGTH);
            double vz = body.z() + body.tiltZ() * (y - yc) + along * sin + across * cos;
            if (Math.hypot(x - vx, z - vz) < vent.radius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether a block lies in one of the deposit's porphyry dikes.
     *
     * @param body the deposit
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return true inside a dike
     */
    private boolean inDike(Body body, int x, int y, int z) {
        double yc = centreY(body);
        double dx = x - body.x() - body.tiltX() * (y - yc);
        double dz = z - body.z() - body.tiltZ() * (y - yc);
        for (Body.Dike dike : body.dikes()) {
            double across = dz * Math.cos(dike.angle()) - dx * Math.sin(dike.angle());
            if (Math.abs(across - dike.offset() * body.radius()) < dike.width() / 2.0) {
                return true;
            }
        }
        return false;
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
