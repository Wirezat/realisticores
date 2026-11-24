package de.wirezat.realisticores.kimberlite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        int centerX = (int) ((cellX + 0.5 + (rng.nextDouble() * 2.0 - 1.0) * f.jitter()) * f.cellSize());
        int centerZ = (int) ((cellZ + 0.5 + (rng.nextDouble() * 2.0 - 1.0) * f.jitter()) * f.cellSize());
        if (!craton.contains(seed, centerX, centerZ)) {
            return Optional.empty();
        }
        int radius = f.minRadius() + (int) (rng.nextDouble() * (f.maxRadius() - f.minRadius() + 1));
        int attempts = f.minPipes() + (int) (rng.nextDouble() * (f.maxPipes() - f.minPipes() + 1));
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            placePipe(rng, centerX, centerZ, radius, pipes);
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
        double reach = f.maxRadius() + shape.reach(new Pipe(0, 0, params.pipe().maxDiameter()));
        int firstX = Math.floorDiv((int) (minX - reach), f.cellSize());
        int lastX = Math.floorDiv((int) (maxX + reach), f.cellSize());
        int firstZ = Math.floorDiv((int) (minZ - reach), f.cellSize());
        int lastZ = Math.floorDiv((int) (maxZ + reach), f.cellSize());
        List<Pipe> result = new ArrayList<>();
        for (int cellX = firstX; cellX <= lastX; cellX++) {
            for (int cellZ = firstZ; cellZ <= lastZ; cellZ++) {
                fieldAt(seed, cellX, cellZ).ifPresent(field -> {
                    for (Pipe pipe : field.pipes()) {
                        double r = shape.reach(pipe);
                        if (pipe.x() + r >= minX && pipe.x() - r <= maxX
                                && pipe.z() + r >= minZ && pipe.z() - r <= maxZ) {
                            result.add(pipe);
                        }
                    }
                });
            }
        }
        return result;
    }

    private void placePipe(SplitMix64 rng, int centerX, int centerZ, int radius, List<Pipe> placed) {
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
                placed.add(new Pipe(x, z, diameter));
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
        int cell = params.field().cellSize();
        int originX = Math.floorDiv(x, cell);
        int originZ = Math.floorDiv(z, cell);
        int lastRing = maxRadius / cell + 1;
        Field best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int ring = 0; ring <= lastRing; ring++) {
            if (best != null && bestDistance <= (ring - 1) * (double) cell) {
                break;
            }
            for (int cellX = originX - ring; cellX <= originX + ring; cellX++) {
                for (int cellZ = originZ - ring; cellZ <= originZ + ring; cellZ++) {
                    if (Math.max(Math.abs(cellX - originX), Math.abs(cellZ - originZ)) != ring) {
                        continue;
                    }
                    Optional<Field> field = fieldAt(seed, cellX, cellZ).filter(f -> !f.pipes().isEmpty());
                    if (field.isPresent()) {
                        double distance = Math.hypot(field.get().centerX() - x, field.get().centerZ() - z);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = field.get();
                        }
                    }
                }
            }
        }
        return bestDistance <= maxRadius ? Optional.ofNullable(best) : Optional.empty();
    }
}
