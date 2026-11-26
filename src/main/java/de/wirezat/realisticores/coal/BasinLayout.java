package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.SplitMix64;
import java.util.ArrayList;
import java.util.List;

/**
 * Inner structure of a basin.
 *
 * @param depth depth of the bowl at the centre in blocks
 * @param seams seams from bottom to top
 * @param faults faults that cut the basin
 * @param foldAmplitude height of the folds in blocks, 0 without folds
 * @param foldWavelength distance between two folds in blocks
 * @param foldPhase phase of the folds in radians
 */
public record BasinLayout(double depth, List<Seam> seams, List<Fault> faults, double foldAmplitude,
        double foldWavelength, double foldPhase) {
    private static final int SALT = 2;
    private static final double FAULT_SPREAD = 0.7;

    /**
     * One seam.
     *
     * @param offset height of the seam base above the basin floor in blocks
     * @param thickness thickness before noise in blocks
     */
    public record Seam(double offset, double thickness) {
    }

    /**
     * One fault line.
     *
     * @param normalX X part of the unit normal
     * @param normalZ Z part of the unit normal
     * @param distance distance of the line from the basin centre in blocks
     * @param offset vertical shift of everything beyond the line in blocks
     */
    public record Fault(double normalX, double normalZ, double distance, double offset) {
    }

    /**
     * Rolls the structure of a basin.
     *
     * @param basin the basin
     * @param type type of the basin after the terrain was measured
     * @param params generation settings
     * @return the structure; seams that would not stay below the cover are left out from the top
     */
    public static BasinLayout of(Basin basin, BasinType type, CoalParams params) {
        CoalParams.TypeParams t = params.type(type);
        SplitMix64 rng = new SplitMix64(SplitMix64.hash(basin.seed(), SALT, type.ordinal()));
        double depth = rng.nextDouble(t.minDepth(), t.maxDepth());
        int count = rng.nextInt(t.minSeams(), t.maxSeams());
        List<Seam> seams = new ArrayList<>();
        double offset = 0.0;
        for (int i = 0; i < count; i++) {
            double thickness = rng.nextDouble(t.minThickness(), t.maxThickness());
            if (offset + thickness > depth - params.seam().cover()) {
                break;
            }
            seams.add(new Seam(offset, thickness));
            offset += thickness + rng.nextDouble(t.minSpacing(), t.maxSpacing());
        }
        double radius = Math.sqrt(basin.longRadius() * basin.shortRadius());
        List<Fault> faults = new ArrayList<>();
        for (int i = rng.nextInt(0, t.maxFaults()); i > 0; i--) {
            double normal = basin.strike() + (rng.nextDouble() < 0.5 ? 0.0 : Math.PI / 2.0);
            double distance = rng.nextDouble(-1.0, 1.0) * FAULT_SPREAD * radius;
            double size = rng.nextDouble(params.seam().minThrow(), params.seam().maxThrow());
            faults.add(new Fault(Math.cos(normal), Math.sin(normal), distance, rng.nextDouble() < 0.5 ? size : -size));
        }
        CoalParams.FoldParams f = params.fold();
        double amplitude = type == BasinType.FOLDED ? rng.nextDouble(f.minAmplitude(), f.maxAmplitude()) : 0.0;
        return new BasinLayout(depth, List.copyOf(seams), List.copyOf(faults), amplitude,
                rng.nextDouble(f.minWavelength(), f.maxWavelength()), rng.nextDouble() * 2.0 * Math.PI);
    }
}
