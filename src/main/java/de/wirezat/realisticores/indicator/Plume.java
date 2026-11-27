package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/** Traces the downhill trail of indicator grains from a deposit and tells how dense the grains are at a position. */
public final class Plume {
    private static final long SALT = 0x504C554D455F4B49L;
    private static final double SPOT_MIN_LEAD = 0.02;

    private Plume() {
    }

    /**
     * Gives the regional flow direction used where the terrain is flat. Deposits in the same area share it.
     *
     * @param seed world seed
     * @param source the deposit
     * @param areaSize edge of the area in blocks that shares one direction
     * @return heading in radians
     */
    public static double fallbackAngle(long seed, TrailSource source, int areaSize) {
        long hash = SplitMix64.hash(seed ^ SALT, Math.floorDiv(source.x(), areaSize),
                Math.floorDiv(source.z(), areaSize));
        return new SplitMix64(hash).nextDouble() * 2.0 * Math.PI;
    }

    /**
     * Gives the length factor of a deposit from the climate at its centre.
     *
     * @param source the deposit
     * @param probe terrain and climate source
     * @param params trail settings
     * @return factor applied to the trail length and the mineral spread
     */
    public static double lengthFactor(TrailSource source, TerrainProbe probe, PlumeParams params) {
        return switch (probe.climate(source.x(), source.z())) {
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
    public static double maxFactor(PlumeParams params) {
        return Math.max(1.0, Math.max(params.coldFactor(), Math.max(params.dryFactor(), params.oceanFactor())));
    }

    /**
     * Follows the terrain downhill from the deposit until the trail is long enough or leaves the shallow sea floor.
     *
     * @param source the deposit
     * @param <S> kind of deposit
     * @param fallbackAngle heading in radians used where the terrain is flat
     * @param probe terrain height source
     * @param seaLevel Y of the sea surface; a trail ends where the terrain lies deeper below it than the depth limit
     * @param params trail settings
     * @return the path from the deposit centre to its end
     */
    public static <S extends TrailSource> PlumePath<S> trace(S source, double fallbackAngle, TerrainProbe probe,
            int seaLevel, PlumeParams params) {
        double factor = lengthFactor(source, probe, params);
        int capacity = (int) Math.ceil(params.maxLength() * factor / params.stepLength()) + 1;
        double[] xs = new double[capacity];
        double[] zs = new double[capacity];
        double[] lengths = new double[capacity];
        xs[0] = source.x();
        zs[0] = source.z();
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
        return new PlumePath<>(source, Arrays.copyOf(xs, count), Arrays.copyOf(zs, count),
                Arrays.copyOf(lengths, count), factor);
    }

    /**
     * Reads the terrain height at a point of a trail.
     *
     * @param probe terrain source
     * @param x world X
     * @param z world Z
     * @return Y of the top solid block
     */
    private static double height(TerrainProbe probe, double x, double z) {
        return probe.terrainHeight((int) Math.round(x), (int) Math.round(z));
    }

    /**
     * Gives how dense a mineral is at a position.
     *
     * @param path trail of the deposit
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param indicator spread of the mineral
     * @param params trail settings
     * @return density from 0 to 1; it falls with the distance along the trail and from its centre line
     */
    public static double density(PlumePath<?> path, double x, double z,
            PlumeParams params, PlumeParams.IndicatorParams indicator) {
        return density(path, nearest(path, x, z), params, indicator);
    }

    /**
     * Gives how dense every indicator mineral is at a position.
     *
     * @param path trail of the deposit
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param params trail settings
     * @return density from 0 to 1 by indicator id, in the order of the indicators
     */
    public static Map<String, Double> densities(PlumePath<?> path, double x, double z,
            PlumeParams params) {
        Nearest nearest = nearest(path, x, z);
        Map<String, Double> densities = new LinkedHashMap<>();
        for (PlumeParams.IndicatorParams indicator : params.indicators()) {
            densities.put(indicator.id(), density(path, nearest, params, indicator));
        }
        return densities;
    }

    /**
     * Gives how dense a mineral is, given the nearest point of the trail.
     *
     * @param path trail of the deposit
     * @param nearest nearest point of the trail to the position
     * @param params trail settings
     * @param indicator spread of the mineral
     * @return density from 0 to 1
     */
    private static double density(PlumePath<?> path, Nearest nearest, PlumeParams params,
            PlumeParams.IndicatorParams indicator) {
        double outside = Math.max(0.0, nearest.distance() - path.source().outcrop());
        double width = params.startWidth() + params.widthGrowth() * nearest.length();
        double decay = nearest.length() == 0.0 ? 1.0
                : Math.exp(-nearest.length() / (indicator.length() * path.factor()));
        return indicator.peak() * decay * Math.exp(-outside * outside / (2.0 * width * width));
    }

    /**
     * A point a map can mark.
     *
     * @param x world X in blocks
     * @param z world Z in blocks
     */
    public record Spot(double x, double z) {
    }

    /**
     * Lists the trail points a map may mark: on land, rich enough in the lead indicator and away from every deposit.
     *
     * @param path trail of the deposit
     * @param sources all deposits near the trail
     * @param minDistance smallest distance to the edge of every deposit in blocks
     * @param probe terrain height source
     * @param seaLevel Y of the sea surface
     * @param params trail settings
     * @return the points from the deposit to the end of the trail, possibly none
     */
    public static List<Spot> mapSpots(PlumePath<?> path, List<? extends TrailSource> sources, double minDistance,
            TerrainProbe probe, int seaLevel, PlumeParams params) {
        List<Spot> spots = new ArrayList<>();
        for (int i = 0; i < path.xs().length; i++) {
            double x = path.xs()[i];
            double z = path.zs()[i];
            if (density(path, x, z, params, params.lead()) >= SPOT_MIN_LEAD
                    && sources.stream().allMatch(source -> Math.hypot(x - source.x(), z - source.z())
                            >= minDistance + source.outcrop())
                    && height(probe, x, z) >= seaLevel - 1) {
                spots.add(new Spot(x, z));
            }
        }
        return spots;
    }

    /**
     * Keeps the points where the strongest trail belongs to a wanted deposit.
     *
     * @param spots the points
     * @param paths all trails that may reach them
     * @param params trail settings
     * @param wanted tells which deposits a map may lead to
     * @param <S> kind of deposit
     * @return the points whose strongest trail comes from a wanted deposit
     */
    public static <S extends TrailSource> List<Spot> keepFrom(List<Spot> spots, List<PlumePath<S>> paths,
            PlumeParams params, Predicate<? super S> wanted) {
        return spots.stream().filter(spot -> {
            PlumePath<S> strongest = strongest(paths, spot.x(), spot.z(), params);
            return strongest != null && wanted.test(strongest.source());
        }).toList();
    }

    /**
     * Gives the trail with the most grains of the lead indicator at a position.
     *
     * @param paths trails that may reach the position
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param params trail settings
     * @param <S> kind of deposit
     * @return the strongest trail, or null without trails
     */
    @Nullable
    public static <S extends TrailSource> PlumePath<S> strongest(List<PlumePath<S>> paths, double x, double z,
            PlumeParams params) {
        double most = 0.0;
        PlumePath<S> strongest = null;
        for (PlumePath<S> path : paths) {
            double lead = density(path, x, z, params, params.lead());
            if (lead > most) {
                most = lead;
                strongest = path;
            }
        }
        return strongest;
    }

    private record Nearest(int segment, double fraction, double distance, double length) {
    }

    /**
     * Finds the point of a trail nearest to a position.
     *
     * @param path the trail
     * @param x world X
     * @param z world Z
     * @return segment, position on it, distance and length along the trail
     */
    private static Nearest nearest(PlumePath<?> path, double x, double z) {
        int segment = -1;
        double fraction = 0.0;
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
                segment = i;
                fraction = clamped;
                bestLength = path.lengths()[i] + clamped * (path.lengths()[i + 1] - path.lengths()[i]);
            }
        }
        return new Nearest(segment, fraction, bestDistance, bestLength);
    }

    /**
     * Lists evenly spaced points from a position to the nearest trail point and on along the trail back toward the
     * deposit.
     *
     * @param path trail of the deposit
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param reach distance in blocks to cover, counted from the position
     * @param spacing distance in blocks between two points
     * @return points ordered from the position toward the deposit, empty if the trail has no segment
     */
    public static List<GuidePoint> guide(PlumePath<?> path, double x, double z, double reach, double spacing) {
        List<GuidePoint> points = new ArrayList<>();
        if (path.xs().length < 2) {
            return points;
        }
        Nearest nearest = nearest(path, x, z);
        int segment = Math.max(0, nearest.segment());
        List<double[]> waypoints = new ArrayList<>();
        waypoints.add(new double[] {x, z});
        waypoints.add(new double[] {
            path.xs()[segment] + (path.xs()[segment + 1] - path.xs()[segment]) * nearest.fraction(),
            path.zs()[segment] + (path.zs()[segment + 1] - path.zs()[segment]) * nearest.fraction()});
        for (int i = segment; i >= 0; i--) {
            waypoints.add(new double[] {path.xs()[i], path.zs()[i]});
        }
        double carry = 0.0;
        double covered = 0.0;
        for (int i = 1; i < waypoints.size() && covered <= reach; i++) {
            double[] from = waypoints.get(i - 1);
            double dx = waypoints.get(i)[0] - from[0];
            double dz = waypoints.get(i)[1] - from[1];
            double length = Math.hypot(dx, dz);
            if (length > 0.0) {
                while (carry <= length && covered + carry <= reach) {
                    points.add(new GuidePoint(from[0] + dx / length * carry, from[1] + dz / length * carry,
                            dx / length, dz / length));
                    carry += spacing;
                }
                carry -= length;
                covered += length;
            }
        }
        return points;
    }
}
