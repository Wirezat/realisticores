package de.wirezat.realisticores.kimberlite;

import java.util.Arrays;

/** Traces the downhill trail of indicator grains from a pipe and tells how dense the grains are at a position. */
public final class Plume {
    private static final long SALT = 0x504C554D455F4B49L;

    private Plume() {
    }

    /**
     * Gives the regional flow direction used where the terrain is flat. Pipes in the same area share it.
     *
     * @param seed world seed
     * @param pipe the pipe
     * @param areaSize edge of the area in blocks that shares one direction
     * @return heading in radians
     */
    public static double fallbackAngle(long seed, Pipe pipe, int areaSize) {
        long hash = SplitMix64.hash(seed ^ SALT, Math.floorDiv(pipe.x(), areaSize), Math.floorDiv(pipe.z(), areaSize));
        return new SplitMix64(hash).nextDouble() * 2.0 * Math.PI;
    }

    /**
     * Gives the length factor of a pipe from the climate at its centre.
     *
     * @param pipe the pipe
     * @param probe terrain and climate source
     * @param params trail settings
     * @return factor applied to the trail length and the mineral spread
     */
    public static double lengthFactor(Pipe pipe, TerrainProbe probe, KimberliteParams.PlumeParams params) {
        return switch (probe.climate(pipe.x(), pipe.z())) {
            case COLD -> params.coldFactor();
            case DRY -> params.dryFactor();
            case OCEAN -> params.oceanFactor();
            case TEMPERATE -> 1.0;
        };
    }

    /**
     * Gives the largest length factor any climate can apply.
     *
     * @param params trail settings
     * @return the largest of the climate factors and 1
     */
    public static double maxFactor(KimberliteParams.PlumeParams params) {
        return Math.max(1.0, Math.max(params.coldFactor(), Math.max(params.dryFactor(), params.oceanFactor())));
    }

    /**
     * Follows the terrain downhill from the pipe until the trail is long enough or leaves the shallow sea floor.
     *
     * @param pipe the source pipe
     * @param fallbackAngle heading in radians used where the terrain is flat
     * @param probe terrain height source
     * @param seaLevel Y of the sea surface; a trail ends where the terrain lies deeper below it than the depth limit
     * @param params trail settings
     * @return the path from the pipe centre to its end
     */
    public static PlumePath trace(Pipe pipe, double fallbackAngle, TerrainProbe probe, int seaLevel,
            KimberliteParams.PlumeParams params) {
        double factor = lengthFactor(pipe, probe, params);
        int capacity = (int) Math.ceil(params.maxLength() * factor / params.stepLength()) + 1;
        double[] xs = new double[capacity];
        double[] zs = new double[capacity];
        double[] lengths = new double[capacity];
        xs[0] = pipe.x();
        zs[0] = pipe.z();
        double headingX = Math.cos(fallbackAngle);
        double headingZ = Math.sin(fallbackAngle);
        double sample = params.stepLength();
        double centerHeight = height(probe, xs[0], zs[0]);
        int count = 1;
        while (count < capacity) {
            double x = xs[count - 1];
            double z = zs[count - 1];
            double slopeX = (height(probe, x + sample, z) - centerHeight) / sample;
            double slopeZ = (height(probe, x, z + sample) - centerHeight) / sample;
            double slope = Math.hypot(slopeX, slopeZ);
            double pull = Math.min(1.0, slope / params.flatSlope());
            double downX = slope == 0.0 ? 0.0 : -slopeX / slope;
            double downZ = slope == 0.0 ? 0.0 : -slopeZ / slope;
            double wantX = downX * pull + Math.cos(fallbackAngle) * (1.0 - pull);
            double wantZ = downZ * pull + Math.sin(fallbackAngle) * (1.0 - pull);
            double inertia = count == 1 ? 0.0 : params.inertia();
            double nextX = headingX * inertia + wantX * (1.0 - inertia);
            double nextZ = headingZ * inertia + wantZ * (1.0 - inertia);
            double norm = Math.hypot(nextX, nextZ);
            if (norm > 0.0) {
                headingX = nextX / norm;
                headingZ = nextZ / norm;
            }
            xs[count] = x + headingX * params.stepLength();
            zs[count] = z + headingZ * params.stepLength();
            lengths[count] = lengths[count - 1] + params.stepLength();
            count++;
            centerHeight = height(probe, xs[count - 1], zs[count - 1]);
            if (centerHeight < seaLevel - params.maxSeaDepth()) {
                break;
            }
        }
        return new PlumePath(pipe, Arrays.copyOf(xs, count), Arrays.copyOf(zs, count),
                Arrays.copyOf(lengths, count), factor);
    }

    private static double height(TerrainProbe probe, double x, double z) {
        return probe.terrainHeight((int) Math.round(x), (int) Math.round(z));
    }

    /**
     * Gives the chance of finding a mineral by panning at a position.
     *
     * @param path trail of the pipe
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param indicator spread of the mineral
     * @param params trail settings
     * @return chance from 0 to 1; it falls with the distance along the trail and from its centre line
     */
    public static double density(PlumePath path, double x, double z,
            KimberliteParams.PlumeParams params, KimberliteParams.IndicatorParams indicator) {
        Nearest nearest = nearest(path, x, z);
        double outside = Math.max(0.0, nearest.distance() - path.pipe().diameter() / 2.0);
        double width = params.startWidth() + params.widthGrowth() * nearest.length();
        double decay = nearest.length() == 0.0 ? 1.0
                : Math.exp(-nearest.length() / (indicator.length() * path.factor()));
        return indicator.peak() * decay * Math.exp(-outside * outside / (2.0 * width * width));
    }

    private record Nearest(double distance, double length) {
    }

    private static Nearest nearest(PlumePath path, double x, double z) {
        double bestDistance = Math.hypot(x - path.xs()[0], z - path.zs()[0]);
        double bestLength = 0.0;
        for (int i = 0; i < path.xs().length - 1; i++) {
            double dx = path.xs()[i + 1] - path.xs()[i];
            double dz = path.zs()[i + 1] - path.zs()[i];
            double along = ((x - path.xs()[i]) * dx + (z - path.zs()[i]) * dz) / (dx * dx + dz * dz);
            double clamped = Math.max(0.0, Math.min(1.0, along));
            double distance = Math.hypot(x - (path.xs()[i] + dx * clamped), z - (path.zs()[i] + dz * clamped));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestLength = path.lengths()[i] + clamped * (path.lengths()[i + 1] - path.lengths()[i]);
            }
        }
        return new Nearest(bestDistance, bestLength);
    }
}
