package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.Cells;
import de.wirezat.realisticores.worldgen.SplitMix64;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Derives kimberlite fields and their pipes deterministically from the world seed. */
public final class FieldGenerator {
    private static final long SALT = 0x4649454C445F4B49L;

    private final KimberliteParams params;
    private final CratonMask craton;
    private final PipeShape shape;

    /**
     * Creates a generator.
     *
     * @param params generation settings
     */
    public FieldGenerator(KimberliteParams params) {
        this.params = params;
        this.craton = new CratonMask(params.craton());
        this.shape = new PipeShape(params.pipe(), new PipeProfile(params.pipe()));
    }

    /**
     * Builds the field of one grid cell.
     *
     * @param seed world seed
     * @param cellX grid cell X index
     * @param cellZ grid cell Z index
     * @return the field, or empty if the cell holds none or lies outside a craton
     */
    public Optional<Field> fieldAt(long seed, int cellX, int cellZ) {
        KimberliteParams.FieldParams f = params.field();
        SplitMix64 rng = new SplitMix64(SplitMix64.hash(seed ^ SALT, cellX, cellZ));
        if (rng.nextDouble() >= f.chance()) {
            return Optional.empty();
        }
        int centerX = Cells.center(rng, cellX, f.cellSize(), f.jitter());
        int centerZ = Cells.center(rng, cellZ, f.cellSize(), f.jitter());
        if (!craton.contains(seed, centerX, centerZ)) {
            return Optional.empty();
        }
        int radius = rng.nextInt(f.minRadius(), f.maxRadius());
        int attempts = rng.nextInt(f.minPipes(), f.maxPipes());
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            placePipe(seed, rng, centerX, centerZ, radius, pipes);
        }
        return Optional.of(new Field(centerX, centerZ, radius, List.copyOf(pipes)));
    }

    /**
     * Lists all pipes that may touch a block rectangle.
     *
     * @param seed world seed
     * @param minX west edge of the rectangle in blocks
     * @param minZ north edge of the rectangle in blocks
     * @param maxX east edge of the rectangle in blocks
     * @param maxZ south edge of the rectangle in blocks
     * @return pipes whose widest radius reaches the rectangle
     */
    public List<Pipe> pipesNear(long seed, int minX, int minZ, int maxX, int maxZ) {
        KimberliteParams.FieldParams f = params.field();
        double reach = f.maxRadius() + shape.reach(Pipe.of(seed, 0, 0, params.pipe().maxDiameter()));
        List<Pipe> result = new ArrayList<>();
        Cells.around(minX, minZ, maxX, maxZ, reach, f.cellSize(), (cellX, cellZ) ->
                fieldAt(seed, cellX, cellZ).ifPresent(field -> {
                    for (Pipe pipe : field.pipes()) {
                        double r = shape.reach(pipe);
                        if (pipe.x() + r >= minX && pipe.x() - r <= maxX
                                && pipe.z() + r >= minZ && pipe.z() - r <= maxZ) {
                            result.add(pipe);
                        }
                    }
                }));
        return result;
    }

    private void placePipe(long seed, SplitMix64 rng, int centerX, int centerZ, int radius, List<Pipe> placed) {
        KimberliteParams.FieldParams f = params.field();
        KimberliteParams.PipeParams p = params.pipe();
        double lnDiameter = Math.log(p.medianDiameter()) + rng.nextGaussian() * p.diameterSigma();
        int diameter = (int) Math.round(Math.min(p.maxDiameter(), Math.max(p.minDiameter(), Math.exp(lnDiameter))));
        for (int attempt = 0; attempt < f.placementAttempts(); attempt++) {
            double angle = rng.nextDouble() * 2.0 * Math.PI;
            double distance = radius * Math.sqrt(rng.nextDouble());
            int x = centerX + (int) Math.round(Math.cos(angle) * distance);
            int z = centerZ + (int) Math.round(Math.sin(angle) * distance);
            if (placed.stream().allMatch(o -> Math.hypot(o.x() - x, o.z() - z) >= f.minSpacing())) {
                placed.add(Pipe.of(seed, x, z, diameter));
                return;
            }
        }
    }

    /**
     * Finds the kimberlite field whose centre is nearest to a position.
     *
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param maxRadius search limit in blocks
     * @return the nearest field with at least one pipe, empty if none lies within the limit
     */
    public Optional<Field> nearestField(long seed, int x, int z, int maxRadius) {
        return nearest(seed, x, z, maxRadius, 0, field -> field.pipes().isEmpty() ? Optional.empty()
                : Optional.of(new Candidate<>(field, Math.hypot(field.centerX() - x, field.centerZ() - z))));
    }

    /**
     * Finds the pipe whose centre is nearest to a position.
     *
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param maxRadius search limit in blocks
     * @return the nearest pipe, empty if none lies within the limit
     */
    public Optional<Pipe> nearestPipe(long seed, int x, int z, int maxRadius) {
        return nearest(seed, x, z, maxRadius, params.field().maxRadius() + 1, field -> field.pipes().stream()
                .map(pipe -> new Candidate<>(pipe, Math.hypot(pipe.x() - x, pipe.z() - z)))
                .min(Comparator.comparingDouble(Candidate::distance)));
    }

    private record Candidate<T>(T value, double distance) {
    }

    private <T> Optional<T> nearest(long seed, int x, int z, int maxRadius, int slack,
            Function<Field, Optional<Candidate<T>>> measure) {
        int cell = params.field().cellSize();
        int originX = Math.floorDiv(x, cell);
        int originZ = Math.floorDiv(z, cell);
        int lastRing = (maxRadius + slack) / cell + 1;
        Candidate<T> best = null;
        for (int ring = 0; ring <= lastRing; ring++) {
            if (best != null && best.distance() <= (ring - 1) * (double) cell - slack) {
                break;
            }
            for (int cellX = originX - ring; cellX <= originX + ring; cellX++) {
                for (int cellZ = originZ - ring; cellZ <= originZ + ring; cellZ++) {
                    if (Math.max(Math.abs(cellX - originX), Math.abs(cellZ - originZ)) != ring) {
                        continue;
                    }
                    Optional<Candidate<T>> found = fieldAt(seed, cellX, cellZ).flatMap(measure);
                    if (found.isPresent() && (best == null || found.get().distance() < best.distance())) {
                        best = found.get();
                    }
                }
            }
        }
        return best != null && best.distance() <= maxRadius ? Optional.of(best.value()) : Optional.empty();
    }
}
