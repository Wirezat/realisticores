package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.worldgen.Cells;
import de.wirezat.realisticores.worldgen.Memo;
import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Derives groups of porphyry copper deposits from the world seed, only where the terrain is high. */
public final class ClusterGenerator {
    private static final long SALT = 0x504F52504859525AL;
    private static final int CACHE_LIMIT = 4096;
    private static final double MAX_ELONGATION = 1.6;
    private static final double SLIGHT_SHARE = 0.25;
    private static final double MODERATE_SHARE = 0.75;

    private final PorphyryParams params;
    private final Memo<CellKey, List<Body>> cells = new Memo<>(CACHE_LIMIT);

    private record CellKey(long seed, int x, int z) {
    }

    /**
     * Creates a generator.
     *
     * @param params placement settings
     */
    public ClusterGenerator(PorphyryParams params) {
        this.params = params;
    }

    /**
     * Gives the deposits of one grid cell, measuring the terrain on first use.
     *
     * @param seed world seed
     * @param probe reader for the original terrain
     * @param cellX grid cell X index
     * @param cellZ grid cell Z index
     * @return deposits of the cell's group, empty if the cell holds none
     */
    public List<Body> bodiesAt(long seed, TerrainProbe probe, int cellX, int cellZ) {
        return cells.get(new CellKey(seed, cellX, cellZ), key -> group(seed, probe, cellX, cellZ));
    }

    /**
     * Builds the group of one cell: the first deposit near the cell centre, each further one at group spacing
     * from a deposit already placed.
     *
     * @param seed world seed
     * @param probe reader for the original terrain
     * @param cellX grid cell X index
     * @param cellZ grid cell Z index
     * @return deposits that found high enough ground
     */
    private List<Body> group(long seed, TerrainProbe probe, int cellX, int cellZ) {
        SplitMix64 rng = new SplitMix64(SplitMix64.hash(seed ^ SALT, cellX, cellZ));
        if (rng.nextDouble() >= params.chance()) {
            return List.of();
        }
        int centerX = Cells.center(rng, cellX, params.cellSize(), params.jitter());
        int centerZ = Cells.center(rng, cellZ, params.cellSize(), params.jitter());
        int count = params.deposits().pick(rng);
        List<Body> bodies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < params.placementAttempts(); attempt++) {
                int x = centerX;
                int z = centerZ;
                if (!bodies.isEmpty() || attempt > 0) {
                    Body anchor = bodies.isEmpty() ? null : bodies.get(rng.nextInt(0, bodies.size() - 1));
                    double angle = rng.nextDouble() * 2.0 * Math.PI;
                    int distance = params.spacing().pick(rng);
                    x = (anchor == null ? centerX : anchor.x()) + (int) Math.round(Math.cos(angle) * distance);
                    z = (anchor == null ? centerZ : anchor.z()) + (int) Math.round(Math.sin(angle) * distance);
                }
                if (!spaced(bodies, x, z)) {
                    continue;
                }
                int height = probe.terrainHeight(x, z);
                if (height >= params.minHeight()) {
                    bodies.add(new Body(x, z, params.radius().pick(rng), rng.nextDouble(1.0, MAX_ELONGATION),
                            rng.nextDouble() * Math.PI, stage(rng.nextDouble()), height, rng.nextLong()));
                    break;
                }
            }
        }
        return List.copyOf(bodies);
    }

    /**
     * Checks the group spacing.
     *
     * @param bodies deposits placed so far
     * @param x candidate X
     * @param z candidate Z
     * @return true if the candidate keeps the smallest spacing to all of them
     */
    private boolean spaced(List<Body> bodies, int x, int z) {
        return bodies.stream().allMatch(b -> Math.hypot(b.x() - x, b.z() - z) >= params.spacing().min());
    }

    /**
     * Picks an erosion stage: slight 25 %, moderate 50 %, deep 25 %.
     *
     * @param roll uniform value in [0, 1)
     * @return the stage
     */
    private static Stage stage(double roll) {
        return roll < SLIGHT_SHARE ? Stage.SLIGHT : roll < MODERATE_SHARE ? Stage.MODERATE : Stage.DEEP;
    }

    /**
     * Gives how far a deposit may lie from its cell centre.
     *
     * @return slack in blocks
     */
    private int slack() {
        return params.spacing().max() * params.deposits().max();
    }

    /**
     * Lists the deposits that may touch a block rectangle.
     *
     * @param seed world seed
     * @param probe reader for the original terrain
     * @param minX west edge in blocks
     * @param minZ north edge in blocks
     * @param maxX east edge in blocks
     * @param maxZ south edge in blocks
     * @return deposits whose reach overlaps the rectangle
     */
    public List<Body> bodiesNear(long seed, TerrainProbe probe, int minX, int minZ, int maxX, int maxZ) {
        double reach = slack() + PorphyryShape.OUTER * 1.2 * MAX_ELONGATION * params.radius().max();
        List<Body> result = new ArrayList<>();
        Cells.around(minX, minZ, maxX, maxZ, reach, params.cellSize(), (cellX, cellZ) -> {
            for (Body body : bodiesAt(seed, probe, cellX, cellZ)) {
                double r = PorphyryShape.reach(body);
                if (body.x() + r >= minX && body.x() - r <= maxX && body.z() + r >= minZ && body.z() - r <= maxZ) {
                    result.add(body);
                }
            }
        });
        return result;
    }

    /**
     * Finds the deposit whose axis is nearest to a position, searching the cells ring by ring.
     *
     * @param seed world seed
     * @param probe reader for the original terrain
     * @param x world X
     * @param z world Z
     * @param limit search limit in blocks
     * @return the nearest deposit, empty if none lies within the limit
     */
    public Optional<Body> nearest(long seed, TerrainProbe probe, int x, int z, int limit) {
        int cell = params.cellSize();
        int originX = Math.floorDiv(x, cell);
        int originZ = Math.floorDiv(z, cell);
        int slack = slack();
        Body best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int ring = 0; ring <= (limit + slack) / cell + 1; ring++) {
            if (best != null && bestDistance <= (ring - 1) * (double) cell - slack) {
                break;
            }
            for (int cellX = originX - ring; cellX <= originX + ring; cellX++) {
                for (int cellZ = originZ - ring; cellZ <= originZ + ring; cellZ++) {
                    if (Math.max(Math.abs(cellX - originX), Math.abs(cellZ - originZ)) != ring) {
                        continue;
                    }
                    Optional<Body> found = bodiesAt(seed, probe, cellX, cellZ).stream()
                            .min(Comparator.comparingDouble(b -> Math.hypot(b.x() - x, b.z() - z)));
                    if (found.isPresent() && Math.hypot(found.get().x() - x, found.get().z() - z) < bestDistance) {
                        best = found.get();
                        bestDistance = Math.hypot(best.x() - x, best.z() - z);
                    }
                }
            }
        }
        return bestDistance <= limit ? Optional.ofNullable(best) : Optional.empty();
    }
}
