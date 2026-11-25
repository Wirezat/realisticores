package de.wirezat.realisticores.coal;

import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Feature config of the coal basin: where basins lie and what their seams look like. */
public final class CoalConfig {
    public static final ModConfigSpec SPEC;

    private static final Supplier<Integer> BASIN_CELL_SIZE;
    private static final Supplier<Double> BASIN_CHANCE;
    private static final Supplier<Double> BASIN_JITTER;
    private static final Supplier<Integer> BASIN_MIN_RADIUS;
    private static final Supplier<Integer> BASIN_MAX_RADIUS;
    private static final Supplier<Double> BASIN_MIN_ASPECT;
    private static final Supplier<Double> BASIN_OUTLINE_NOISE;
    private static final Supplier<Double> BASIN_OUTLINE_WAVELENGTH;
    private static final Supplier<Double> BASIN_HARD_COAL_SHARE;
    private static final Supplier<Integer> RELIEF_MOUNTAIN_RELIEF;
    private static final Supplier<Integer> RELIEF_FOOTHILL_RISE;
    private static final Supplier<Integer> RELIEF_FOOTHILL_POINTS;
    private static final Supplier<Integer> HARD_COAL_MIN_DEPTH;
    private static final Supplier<Integer> HARD_COAL_MAX_DEPTH;
    private static final Supplier<Integer> HARD_COAL_MIN_SEAMS;
    private static final Supplier<Integer> HARD_COAL_MAX_SEAMS;
    private static final Supplier<Double> HARD_COAL_MIN_THICKNESS;
    private static final Supplier<Double> HARD_COAL_MAX_THICKNESS;
    private static final Supplier<Integer> HARD_COAL_MIN_SPACING;
    private static final Supplier<Integer> HARD_COAL_MAX_SPACING;
    private static final Supplier<Integer> HARD_COAL_MAX_FAULTS;
    private static final Supplier<Integer> LIGNITE_MIN_DEPTH;
    private static final Supplier<Integer> LIGNITE_MAX_DEPTH;
    private static final Supplier<Integer> LIGNITE_MIN_SEAMS;
    private static final Supplier<Integer> LIGNITE_MAX_SEAMS;
    private static final Supplier<Double> LIGNITE_MIN_THICKNESS;
    private static final Supplier<Double> LIGNITE_MAX_THICKNESS;
    private static final Supplier<Integer> LIGNITE_MIN_SPACING;
    private static final Supplier<Integer> LIGNITE_MAX_SPACING;
    private static final Supplier<Integer> LIGNITE_MAX_FAULTS;
    private static final Supplier<Integer> FOLDED_MIN_DEPTH;
    private static final Supplier<Integer> FOLDED_MAX_DEPTH;
    private static final Supplier<Integer> FOLDED_MIN_SEAMS;
    private static final Supplier<Integer> FOLDED_MAX_SEAMS;
    private static final Supplier<Double> FOLDED_MIN_THICKNESS;
    private static final Supplier<Double> FOLDED_MAX_THICKNESS;
    private static final Supplier<Integer> FOLDED_MIN_SPACING;
    private static final Supplier<Integer> FOLDED_MAX_SPACING;
    private static final Supplier<Integer> FOLDED_MAX_FAULTS;
    private static final Supplier<Double> FOLD_MIN_AMPLITUDE;
    private static final Supplier<Double> FOLD_MAX_AMPLITUDE;
    private static final Supplier<Double> FOLD_MIN_WAVELENGTH;
    private static final Supplier<Double> FOLD_MAX_WAVELENGTH;
    private static final Supplier<Double> FOLD_CORE_SHARE;
    private static final Supplier<Double> SEAM_COVER;
    private static final Supplier<Double> SEAM_THICKNESS_BASE;
    private static final Supplier<Double> SEAM_THICKNESS_NOISE;
    private static final Supplier<Double> SEAM_THICKNESS_WAVELENGTH;
    private static final Supplier<Double> SEAM_PINCH_OUT;
    private static final Supplier<Double> SEAM_MIN_THICKNESS;
    private static final Supplier<Double> SEAM_WAVE_AMPLITUDE;
    private static final Supplier<Double> SEAM_WAVE_WAVELENGTH;
    private static final Supplier<Double> SEAM_SPLIT_THRESHOLD;
    private static final Supplier<Double> SEAM_SPLIT_WAVELENGTH;
    private static final Supplier<Integer> SEAM_MAX_PARTING;
    private static final Supplier<Double> SEAM_MIN_THROW;
    private static final Supplier<Double> SEAM_MAX_THROW;
    private static final Supplier<Double> SEAM_FAULT_WAVE_AMPLITUDE;
    private static final Supplier<Double> SEAM_FAULT_WAVE_WAVELENGTH;
    private static final Supplier<Double> SEAM_OUTCROP_THRESHOLD;
    private static final Supplier<Double> SEAM_OUTCROP_WAVELENGTH;
    private static final Supplier<Integer> ROCK_MIN_SHALE;
    private static final Supplier<Integer> ROCK_MAX_SHALE;
    private static final Supplier<Integer> ROCK_MIN_BLACK_SHALE;
    private static final Supplier<Integer> ROCK_MAX_BLACK_SHALE;
    private static final Supplier<Integer> ROCK_MIN_FIRECLAY;
    private static final Supplier<Integer> ROCK_MAX_FIRECLAY;
    private static final Supplier<Integer> ROCK_MIN_ROOF_MUDSTONE;
    private static final Supplier<Integer> ROCK_MAX_ROOF_MUDSTONE;
    private static final Supplier<Integer> ROCK_MIN_FLOOR_MUDSTONE;
    private static final Supplier<Integer> ROCK_MAX_FLOOR_MUDSTONE;
    private static final Supplier<Double> ROCK_WAVELENGTH;

    static {
        CoalParams d = CoalParams.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Position, size and outline of basins.").push("basin");
        BASIN_CELL_SIZE = integer(b, "cellSize", d.basin().cellSize(), 256, 100000,
                "Grid cell size in blocks; each cell holds at most one basin.");
        BASIN_CHANCE = decimal(b, "chance", d.basin().chance(), 0.0, 1.0,
                "Probability that a cell holds a basin.");
        BASIN_JITTER = decimal(b, "jitter", d.basin().jitter(), 0.0, 0.4,
                "Maximum offset of the centre from the cell centre, as a fraction of the cell size.");
        BASIN_MIN_RADIUS = integer(b, "minRadius", d.basin().minRadius(), 16, 20000,
                "Smallest mean radius in blocks.");
        BASIN_MAX_RADIUS = integer(b, "maxRadius", d.basin().maxRadius(), 16, 20000,
                "Largest mean radius in blocks.");
        BASIN_MIN_ASPECT = decimal(b, "minAspect", d.basin().minAspect(), 0.2, 1.0,
                "Smallest ratio of short to long axis.");
        BASIN_OUTLINE_NOISE = decimal(b, "outlineNoise", d.basin().outlineNoise(), 0.0, 0.5,
                "Fraction of the radius the outline may deviate by noise.");
        BASIN_OUTLINE_WAVELENGTH = decimal(b, "outlineWavelength", d.basin().outlineWavelength(), 1.0, 5000.0,
                "Size of the outline bumps in blocks.");
        BASIN_HARD_COAL_SHARE = decimal(b, "hardCoalShare", d.basin().hardCoalShare(), 0.0, 1.0,
                "Share of basins with hard coal, the rest holds lignite.");
        b.pop();

        b.comment("Terrain limits; applied per basin, never per column.").push("relief");
        RELIEF_MOUNTAIN_RELIEF = integer(b, "mountainRelief", d.relief().mountainRelief(), 0, 2000,
                "Height difference in blocks inside a basin above which there is no basin.");
        RELIEF_FOOTHILL_RISE = integer(b, "foothillRise", d.relief().foothillRise(), 0, 2000,
                "Height in blocks above the basin from which a rim point counts as mountain.");
        RELIEF_FOOTHILL_POINTS = integer(b, "foothillPoints", d.relief().foothillPoints(), 1, 9,
                "Number of mountain points on the rim that make a basin folded.");
        b.pop();

        b.comment("Hard coal basins.").push("hard_coal");
        HARD_COAL_MIN_DEPTH = integer(b, "minDepth", d.hardCoal().minDepth(), 1, 300,
                "Smallest depth of the bowl in blocks.");
        HARD_COAL_MAX_DEPTH = integer(b, "maxDepth", d.hardCoal().maxDepth(), 1, 300,
                "Largest depth of the bowl in blocks.");
        HARD_COAL_MIN_SEAMS = integer(b, "minSeams", d.hardCoal().minSeams(), 1, 10,
                "Fewest seams.");
        HARD_COAL_MAX_SEAMS = integer(b, "maxSeams", d.hardCoal().maxSeams(), 1, 10,
                "Most seams.");
        HARD_COAL_MIN_THICKNESS = decimal(b, "minThickness", d.hardCoal().minThickness(), 0.0, 200.0,
                "Smallest seam thickness in blocks.");
        HARD_COAL_MAX_THICKNESS = decimal(b, "maxThickness", d.hardCoal().maxThickness(), 0.0, 200.0,
                "Largest seam thickness in blocks.");
        HARD_COAL_MIN_SPACING = integer(b, "minSpacing", d.hardCoal().minSpacing(), 0, 200,
                "Smallest gap between two seams in blocks.");
        HARD_COAL_MAX_SPACING = integer(b, "maxSpacing", d.hardCoal().maxSpacing(), 0, 200,
                "Largest gap between two seams in blocks.");
        HARD_COAL_MAX_FAULTS = integer(b, "maxFaults", d.hardCoal().maxFaults(), 0, 20,
                "Most faults.");
        b.pop();

        b.comment("Lignite basins.").push("lignite");
        LIGNITE_MIN_DEPTH = integer(b, "minDepth", d.lignite().minDepth(), 1, 300,
                "Smallest depth of the bowl in blocks.");
        LIGNITE_MAX_DEPTH = integer(b, "maxDepth", d.lignite().maxDepth(), 1, 300,
                "Largest depth of the bowl in blocks.");
        LIGNITE_MIN_SEAMS = integer(b, "minSeams", d.lignite().minSeams(), 1, 10,
                "Fewest seams.");
        LIGNITE_MAX_SEAMS = integer(b, "maxSeams", d.lignite().maxSeams(), 1, 10,
                "Most seams.");
        LIGNITE_MIN_THICKNESS = decimal(b, "minThickness", d.lignite().minThickness(), 0.0, 200.0,
                "Smallest seam thickness in blocks.");
        LIGNITE_MAX_THICKNESS = decimal(b, "maxThickness", d.lignite().maxThickness(), 0.0, 200.0,
                "Largest seam thickness in blocks.");
        LIGNITE_MIN_SPACING = integer(b, "minSpacing", d.lignite().minSpacing(), 0, 200,
                "Smallest gap between two seams in blocks.");
        LIGNITE_MAX_SPACING = integer(b, "maxSpacing", d.lignite().maxSpacing(), 0, 200,
                "Largest gap between two seams in blocks.");
        LIGNITE_MAX_FAULTS = integer(b, "maxFaults", d.lignite().maxFaults(), 0, 20,
                "Most faults.");
        b.pop();

        b.comment("Folded basins at the foot of mountains.").push("folded");
        FOLDED_MIN_DEPTH = integer(b, "minDepth", d.folded().minDepth(), 1, 300,
                "Smallest depth of the bowl in blocks.");
        FOLDED_MAX_DEPTH = integer(b, "maxDepth", d.folded().maxDepth(), 1, 300,
                "Largest depth of the bowl in blocks.");
        FOLDED_MIN_SEAMS = integer(b, "minSeams", d.folded().minSeams(), 1, 10,
                "Fewest seams.");
        FOLDED_MAX_SEAMS = integer(b, "maxSeams", d.folded().maxSeams(), 1, 10,
                "Most seams.");
        FOLDED_MIN_THICKNESS = decimal(b, "minThickness", d.folded().minThickness(), 0.0, 200.0,
                "Smallest seam thickness in blocks.");
        FOLDED_MAX_THICKNESS = decimal(b, "maxThickness", d.folded().maxThickness(), 0.0, 200.0,
                "Largest seam thickness in blocks.");
        FOLDED_MIN_SPACING = integer(b, "minSpacing", d.folded().minSpacing(), 0, 200,
                "Smallest gap between two seams in blocks.");
        FOLDED_MAX_SPACING = integer(b, "maxSpacing", d.folded().maxSpacing(), 0, 200,
                "Largest gap between two seams in blocks.");
        FOLDED_MAX_FAULTS = integer(b, "maxFaults", d.folded().maxFaults(), 0, 20,
                "Most faults.");
        b.pop();

        b.comment("Folds and anthracite core of folded basins.").push("fold");
        FOLD_MIN_AMPLITUDE = decimal(b, "minAmplitude", d.fold().minAmplitude(), 0.0, 200.0,
                "Smallest fold height in blocks.");
        FOLD_MAX_AMPLITUDE = decimal(b, "maxAmplitude", d.fold().maxAmplitude(), 0.0, 200.0,
                "Largest fold height in blocks.");
        FOLD_MIN_WAVELENGTH = decimal(b, "minWavelength", d.fold().minWavelength(), 1.0, 5000.0,
                "Smallest distance between two folds in blocks.");
        FOLD_MAX_WAVELENGTH = decimal(b, "maxWavelength", d.fold().maxWavelength(), 1.0, 5000.0,
                "Largest distance between two folds in blocks.");
        FOLD_CORE_SHARE = decimal(b, "coreShare", d.fold().coreShare(), 0.0, 1.0,
                "Part of the basin radius that holds anthracite.");
        b.pop();

        b.comment("Shape of the seams.").push("seam");
        SEAM_COVER = decimal(b, "cover", d.seam().cover(), 0.0, 100.0,
                "Blocks between the topmost seam and the basin datum at the centre.");
        SEAM_THICKNESS_BASE = decimal(b, "thicknessBase", d.seam().thicknessBase(), 0.0, 5.0,
                "Constant part of the thickness factor.");
        SEAM_THICKNESS_NOISE = decimal(b, "thicknessNoise", d.seam().thicknessNoise(), 0.0, 5.0,
                "Noise part of the thickness factor.");
        SEAM_THICKNESS_WAVELENGTH = decimal(b, "thicknessWavelength", d.seam().thicknessWavelength(), 1.0, 5000.0,
                "Size of thick and thin zones in blocks.");
        SEAM_PINCH_OUT = decimal(b, "pinchOut", d.seam().pinchOut(), 0.01, 1.0,
                "Outer fraction of the basin in which seams thin out.");
        SEAM_MIN_THICKNESS = decimal(b, "minThickness", d.seam().minThickness(), 0.0, 200.0,
                "Thickness in blocks below which there is no seam.");
        SEAM_WAVE_AMPLITUDE = decimal(b, "waveAmplitude", d.seam().waveAmplitude(), 0.0, 100.0,
                "Height of the gentle waves in blocks.");
        SEAM_WAVE_WAVELENGTH = decimal(b, "waveWavelength", d.seam().waveWavelength(), 1.0, 5000.0,
                "Size of the gentle waves in blocks.");
        SEAM_SPLIT_THRESHOLD = decimal(b, "splitThreshold", d.seam().splitThreshold(), -1.0, 2.0,
                "Noise value (-1..1) above which a seam splits.");
        SEAM_SPLIT_WAVELENGTH = decimal(b, "splitWavelength", d.seam().splitWavelength(), 1.0, 5000.0,
                "Size of split zones in blocks.");
        SEAM_MAX_PARTING = integer(b, "maxParting", d.seam().maxParting(), 1, 20,
                "Thickest parting between two benches in blocks.");
        SEAM_MIN_THROW = decimal(b, "minThrow", d.seam().minThrow(), 0.0, 200.0,
                "Smallest shift at a fault in blocks.");
        SEAM_MAX_THROW = decimal(b, "maxThrow", d.seam().maxThrow(), 0.0, 200.0,
                "Largest shift at a fault in blocks.");
        SEAM_FAULT_WAVE_AMPLITUDE = decimal(b, "faultWaveAmplitude", d.seam().faultWaveAmplitude(), 0.0, 200.0,
                "How far a fault line bends in blocks.");
        SEAM_FAULT_WAVE_WAVELENGTH = decimal(b, "faultWaveWavelength", d.seam().faultWaveWavelength(), 1.0, 5000.0,
                "Size of the bends in blocks.");
        SEAM_OUTCROP_THRESHOLD = decimal(b, "outcropThreshold", d.seam().outcropThreshold(), -1.0, 2.0,
                "Noise value (-1..1) above which a seam shows through the soil.");
        SEAM_OUTCROP_WAVELENGTH = decimal(b, "outcropWavelength", d.seam().outcropWavelength(), 1.0, 5000.0,
                "Size of outcrops in blocks.");
        b.pop();

        b.comment("Rocks around a seam.").push("rock");
        ROCK_MIN_SHALE = integer(b, "minShale", d.rock().minShale(), 0, 200,
                "Thinnest shale in blocks.");
        ROCK_MAX_SHALE = integer(b, "maxShale", d.rock().maxShale(), 0, 200,
                "Thickest shale in blocks.");
        ROCK_MIN_BLACK_SHALE = integer(b, "minBlackShale", d.rock().minBlackShale(), 0, 200,
                "Thinnest black shale above a seam in blocks.");
        ROCK_MAX_BLACK_SHALE = integer(b, "maxBlackShale", d.rock().maxBlackShale(), 0, 200,
                "Thickest black shale above a seam in blocks.");
        ROCK_MIN_FIRECLAY = integer(b, "minFireclay", d.rock().minFireclay(), 0, 200,
                "Thinnest fireclay below a seam in blocks.");
        ROCK_MAX_FIRECLAY = integer(b, "maxFireclay", d.rock().maxFireclay(), 0, 200,
                "Thickest fireclay below a seam in blocks.");
        ROCK_MIN_ROOF_MUDSTONE = integer(b, "minRoofMudstone", d.rock().minRoofMudstone(), 0, 200,
                "Thinnest mudstone above lignite in blocks.");
        ROCK_MAX_ROOF_MUDSTONE = integer(b, "maxRoofMudstone", d.rock().maxRoofMudstone(), 0, 200,
                "Thickest mudstone above lignite in blocks.");
        ROCK_MIN_FLOOR_MUDSTONE = integer(b, "minFloorMudstone", d.rock().minFloorMudstone(), 0, 200,
                "Thinnest mudstone below lignite in blocks.");
        ROCK_MAX_FLOOR_MUDSTONE = integer(b, "maxFloorMudstone", d.rock().maxFloorMudstone(), 0, 200,
                "Thickest mudstone below lignite in blocks.");
        ROCK_WAVELENGTH = decimal(b, "wavelength", d.rock().wavelength(), 1.0, 5000.0,
                "Size of thick and thin zones in blocks.");
        b.pop();
        SPEC = b.build();
    }

    private CoalConfig() {
    }

    private static Supplier<Integer> integer(ModConfigSpec.Builder builder, String name, int value, int min, int max,
            String comment) {
        ModConfigSpec.IntValue config = builder.comment(comment).defineInRange(name, value, min, max);
        return config::get;
    }

    private static Supplier<Double> decimal(ModConfigSpec.Builder builder, String name, double value, double min,
            double max, String comment) {
        ModConfigSpec.DoubleValue config = builder.comment(comment).defineInRange(name, value, min, max);
        return config::get;
    }

    /**
     * Reads the current generation parameters from the config.
     *
     * @return parameters with contradicting values repaired
     */
    public static CoalParams params() {
        return new CoalParams(
                new CoalParams.BasinParams(BASIN_CELL_SIZE.get(), BASIN_CHANCE.get(), BASIN_JITTER.get(),
                        BASIN_MIN_RADIUS.get(), BASIN_MAX_RADIUS.get(), BASIN_MIN_ASPECT.get(),
                        BASIN_OUTLINE_NOISE.get(), BASIN_OUTLINE_WAVELENGTH.get(), BASIN_HARD_COAL_SHARE.get()),
                new CoalParams.ReliefParams(RELIEF_MOUNTAIN_RELIEF.get(), RELIEF_FOOTHILL_RISE.get(),
                        RELIEF_FOOTHILL_POINTS.get()),
                new CoalParams.TypeParams(HARD_COAL_MIN_DEPTH.get(), HARD_COAL_MAX_DEPTH.get(),
                        HARD_COAL_MIN_SEAMS.get(), HARD_COAL_MAX_SEAMS.get(), HARD_COAL_MIN_THICKNESS.get(),
                        HARD_COAL_MAX_THICKNESS.get(), HARD_COAL_MIN_SPACING.get(), HARD_COAL_MAX_SPACING.get(),
                        HARD_COAL_MAX_FAULTS.get()),
                new CoalParams.TypeParams(LIGNITE_MIN_DEPTH.get(), LIGNITE_MAX_DEPTH.get(), LIGNITE_MIN_SEAMS.get(),
                        LIGNITE_MAX_SEAMS.get(), LIGNITE_MIN_THICKNESS.get(), LIGNITE_MAX_THICKNESS.get(),
                        LIGNITE_MIN_SPACING.get(), LIGNITE_MAX_SPACING.get(), LIGNITE_MAX_FAULTS.get()),
                new CoalParams.TypeParams(FOLDED_MIN_DEPTH.get(), FOLDED_MAX_DEPTH.get(), FOLDED_MIN_SEAMS.get(),
                        FOLDED_MAX_SEAMS.get(), FOLDED_MIN_THICKNESS.get(), FOLDED_MAX_THICKNESS.get(),
                        FOLDED_MIN_SPACING.get(), FOLDED_MAX_SPACING.get(), FOLDED_MAX_FAULTS.get()),
                new CoalParams.FoldParams(FOLD_MIN_AMPLITUDE.get(), FOLD_MAX_AMPLITUDE.get(),
                        FOLD_MIN_WAVELENGTH.get(), FOLD_MAX_WAVELENGTH.get(), FOLD_CORE_SHARE.get()),
                new CoalParams.SeamParams(SEAM_COVER.get(), SEAM_THICKNESS_BASE.get(), SEAM_THICKNESS_NOISE.get(),
                        SEAM_THICKNESS_WAVELENGTH.get(), SEAM_PINCH_OUT.get(), SEAM_MIN_THICKNESS.get(),
                        SEAM_WAVE_AMPLITUDE.get(), SEAM_WAVE_WAVELENGTH.get(), SEAM_SPLIT_THRESHOLD.get(),
                        SEAM_SPLIT_WAVELENGTH.get(), SEAM_MAX_PARTING.get(), SEAM_MIN_THROW.get(),
                        SEAM_MAX_THROW.get(), SEAM_FAULT_WAVE_AMPLITUDE.get(), SEAM_FAULT_WAVE_WAVELENGTH.get(),
                        SEAM_OUTCROP_THRESHOLD.get(), SEAM_OUTCROP_WAVELENGTH.get()),
                new CoalParams.RockParams(ROCK_MIN_SHALE.get(), ROCK_MAX_SHALE.get(), ROCK_MIN_BLACK_SHALE.get(),
                        ROCK_MAX_BLACK_SHALE.get(), ROCK_MIN_FIRECLAY.get(), ROCK_MAX_FIRECLAY.get(),
                        ROCK_MIN_ROOF_MUDSTONE.get(), ROCK_MAX_ROOF_MUDSTONE.get(), ROCK_MIN_FLOOR_MUDSTONE.get(),
                        ROCK_MAX_FLOOR_MUDSTONE.get(), ROCK_WAVELENGTH.get()))
                .sanitized();
    }
}
