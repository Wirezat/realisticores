package de.wirezat.realisticores;

import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.ore.Deposit;
import de.wirezat.realisticores.ore.Mineral;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Feature config of the kimberlite pipe: where, how big and how rich the pipes are. */
public final class KimberliteConfig {
    public static final ModConfigSpec SPEC;

    private static final Supplier<Double> CRATON_WAVELENGTH;
    private static final Supplier<Double> CRATON_THRESHOLD;
    private static final Supplier<Integer> FIELD_CELL_SIZE;
    private static final Supplier<Double> FIELD_CHANCE;
    private static final Supplier<Double> FIELD_JITTER;
    private static final Supplier<Integer> FIELD_MIN_RADIUS;
    private static final Supplier<Integer> FIELD_MAX_RADIUS;
    private static final Supplier<Integer> FIELD_MIN_PIPES;
    private static final Supplier<Integer> FIELD_MAX_PIPES;
    private static final Supplier<Integer> FIELD_MIN_SPACING;
    private static final Supplier<Integer> FIELD_PLACEMENT_ATTEMPTS;
    private static final Supplier<Integer> PIPE_MEDIAN_DIAMETER;
    private static final Supplier<Double> PIPE_DIAMETER_SIGMA;
    private static final Supplier<Integer> PIPE_MIN_DIAMETER;
    private static final Supplier<Integer> PIPE_MAX_DIAMETER;
    private static final Supplier<Integer> PIPE_CRATER_BASE_Y;
    private static final Supplier<Integer> PIPE_CRATER_HEIGHT;
    private static final Supplier<Double> PIPE_CRATER_WIDENING;
    private static final Supplier<Double> PIPE_DIATREME_TAPER;
    private static final Supplier<Integer> PIPE_ROOT_Y;
    private static final Supplier<Double> PIPE_ROOT_MIN_RADIUS;
    private static final Supplier<Integer> PIPE_BOTTOM_Y;
    private static final Supplier<Double> PIPE_WALL_NOISE;
    private static final Supplier<Double> PIPE_MIN_ASPECT;
    private static final Supplier<Double> PIPE_OUTCROP_THRESHOLD;
    private static final Supplier<Double> PIPE_OUTCROP_WAVELENGTH;
    private static final Supplier<Double> PIPE_WALL_WAVELENGTH_FACTOR;
    private static final Supplier<Double> PIPE_WALL_MIN_WAVELENGTH;
    private static final Supplier<Double> PIPE_WALL_FINE_SHARE;
    private static final Supplier<Double> PIPE_WALL_FINE_SCALE;
    private static final Supplier<Double> PIPE_WALL_VERTICAL_STRETCH;
    private static final Supplier<Double> FACIES_WEATHERING_DEPTH;
    private static final Supplier<Double> FACIES_WEATHERING_NOISE;
    private static final Supplier<Double> FACIES_CONTACT_FRACTION;
    private static final Supplier<Double> FACIES_SERPENTINE_BAND;
    private static final Supplier<Double> FACIES_SERPENTINE_THRESHOLD;
    private static final Supplier<Double> FACIES_XENOLITH_THRESHOLD;
    private static final Supplier<Double> FACIES_PERIDOTITE_THRESHOLD;
    private static final Supplier<Double> FACIES_CONTACT_WAVELENGTH;
    private static final Supplier<Double> FACIES_XENOLITH_WAVELENGTH;
    private static final Supplier<Double> FACIES_PERIDOTITE_WAVELENGTH;
    private static final Supplier<Double> FACIES_SERPENTINE_WAVELENGTH;
    private static final Supplier<Double> FACIES_WEATHERING_WAVELENGTH;
    private static final Supplier<Double> FACIES_SERPENTINE_BAND_BONUS;
    private static final Supplier<Double> FACIES_SERPENTINE_WALL_BONUS;
    private static final Supplier<Double> FACIES_SERPENTINE_WALL_START;
    private static final Supplier<Double> FACIES_CONTACT_THRESHOLD;
    private static final Supplier<Double> CRATER_DEPTH_PER_DIAMETER;
    private static final Supplier<Integer> CRATER_MIN_DEPTH;
    private static final Supplier<Integer> CRATER_MAX_DEPTH;
    private static final Supplier<Integer> CRATER_FLOOR_LAYERS;
    private static final Supplier<Integer> CRATER_RING_POINTS;
    private static final Supplier<Integer> CRATER_MAX_WALL_HEIGHT;
    private static final Supplier<Integer> CRATER_SOIL_LAYERS;
    private static final Supplier<Boolean> CRATER_LAKE_IN_DRY_BIOMES;
    private static final Supplier<Double> GRADE_BARREN_SHARE;
    private static final Supplier<Double> GRADE_POOR_SHARE;
    private static final Supplier<Double> GRADE_POOR_FACTOR;
    private static final Supplier<Double> GRADE_PHASE_WAVELENGTH;
    private static final Supplier<Double> GRADE_PHASE_AMPLITUDE;
    private static final Supplier<Double> PLUME_STEP_LENGTH;
    private static final Supplier<Double> PLUME_MAX_LENGTH;
    private static final Supplier<Double> PLUME_INERTIA;
    private static final Supplier<Double> PLUME_FLAT_SLOPE;
    private static final Supplier<Double> PLUME_START_WIDTH;
    private static final Supplier<Double> PLUME_WIDTH_GROWTH;
    private static final Supplier<Double> PLUME_COLD_FACTOR;
    private static final Supplier<Double> PLUME_DRY_FACTOR;
    private static final Supplier<Double> PLUME_OCEAN_FACTOR;
    private static final Supplier<Double> PLUME_MAX_SEA_DEPTH;
    private static final Supplier<Double> PYROPE_LENGTH;
    private static final Supplier<Double> PYROPE_PEAK;
    private static final Supplier<Double> CHROMITE_LENGTH;
    private static final Supplier<Double> CHROMITE_PEAK;
    private static final Supplier<Double> ILMENITE_LENGTH;
    private static final Supplier<Double> ILMENITE_PEAK;
    private static final Map<String, Supplier<Double>> MINERAL_FRACTIONS = new HashMap<>();

    static {
        KimberliteParams d = KimberliteParams.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Craton mask: only cratons (old, stable crust) hold kimberlite fields.").push("craton");
        CRATON_WAVELENGTH = decimal(b, "wavelength", d.craton().wavelength(), 100.0, 100000.0,
                "Size of craton regions in blocks; larger means fewer, bigger cratons.");
        CRATON_THRESHOLD = decimal(b, "threshold", d.craton().threshold(), -1.0, 2.0,
                "Noise value above which a position is craton; higher means less craton (about 17% at 0.45).");
        b.pop();

        b.comment("Kimberlite fields: clusters of pipes inside cratons.").push("field");
        FIELD_CELL_SIZE = integer(b, "cellSize", d.field().cellSize(), 256, 100000,
                "Grid cell size in blocks; each cell holds at most one field.");
        FIELD_CHANCE = decimal(b, "chance", d.field().chance(), 0.0, 1.0,
                "Probability that a cell on a craton holds a field.");
        FIELD_JITTER = decimal(b, "jitter", d.field().jitter(), 0.0, 0.5,
                "Maximum offset of a field from its cell centre, as a fraction of the cell size.");
        FIELD_MIN_RADIUS = integer(b, "minRadius", d.field().minRadius(), 16, 5000, "Smallest field radius in blocks.");
        FIELD_MAX_RADIUS = integer(b, "maxRadius", d.field().maxRadius(), 16, 5000, "Largest field radius in blocks.");
        FIELD_MIN_PIPES = integer(b, "minPipes", d.field().minPipes(), 1, 200, "Fewest pipes attempted per field.");
        FIELD_MAX_PIPES = integer(b, "maxPipes", d.field().maxPipes(), 1, 200, "Most pipes attempted per field.");
        FIELD_MIN_SPACING = integer(b, "minSpacing", d.field().minSpacing(), 1, 1000,
                "Minimum distance between pipe centres in a field, in blocks.");
        FIELD_PLACEMENT_ATTEMPTS = integer(b, "placementAttempts", d.field().placementAttempts(), 1, 100,
                "Tries to find a free position for each pipe before it is skipped; fewer tries give sparser fields.");
        b.pop();

        b.comment("Pipe size and shape.").push("pipe");
        PIPE_MEDIAN_DIAMETER = integer(b, "medianDiameter", d.pipe().medianDiameter(), 8, 1000,
                "Median surface diameter in blocks.");
        PIPE_DIAMETER_SIGMA = decimal(b, "diameterSigma", d.pipe().diameterSigma(), 0.0, 2.0,
                "Spread of diameters (log-normal); higher gives more very small and very large pipes.");
        PIPE_MIN_DIAMETER = integer(b, "minDiameter", d.pipe().minDiameter(), 4, 1000, "Smallest diameter in blocks.");
        PIPE_MAX_DIAMETER = integer(b, "maxDiameter", d.pipe().maxDiameter(), 8, 2000, "Largest diameter in blocks.");
        PIPE_CRATER_BASE_Y = integer(b, "craterBaseY", d.pipe().craterBaseY(), -64, 320,
                "Height where the crater flare starts.");
        PIPE_CRATER_HEIGHT = integer(b, "craterHeight", d.pipe().craterHeight(), 1, 200,
                "Height of the crater flare in blocks.");
        PIPE_CRATER_WIDENING = decimal(b, "craterWidening", d.pipe().craterWidening(), 1.0, 3.0,
                "Radius factor reached at the top of the crater flare.");
        PIPE_DIATREME_TAPER = decimal(b, "diatremeTaper", d.pipe().diatremeTaper(), 0.0, 0.9,
                "Fraction of the radius lost between the crater base and the root.");
        PIPE_ROOT_Y = integer(b, "rootY", d.pipe().rootY(), -64, 320, "Height where the diatreme turns into the root.");
        PIPE_ROOT_MIN_RADIUS = decimal(b, "rootMinRadius", d.pipe().rootMinRadius(), 0.5, 50.0,
                "Radius in blocks reached at the bottom of the pipe.");
        PIPE_BOTTOM_Y = integer(b, "bottomY", d.pipe().bottomY(), -64, 320, "Lowest height of the pipe.");
        PIPE_WALL_NOISE = decimal(b, "wallNoiseStrength", d.pipe().wallNoiseStrength(), 0.0, 0.8,
                "How strongly the wall deviates from a clean outline, as a fraction of the radius.");
        PIPE_MIN_ASPECT = decimal(b, "minAspect", d.pipe().minAspect(), 0.2, 1.0,
                "Smallest ratio of short to long axis of the outline; 1 means round pipes.");
        PIPE_OUTCROP_THRESHOLD = decimal(b, "outcropThreshold", d.pipe().outcropThreshold(), -1.0, 2.0,
                "Noise value above which kimberlite shows at the surface; higher means fewer outcrops.");
        PIPE_OUTCROP_WAVELENGTH = decimal(b, "outcropWavelength", d.pipe().outcropWavelength(), 2.0, 200.0,
                "Size of outcrop patches in blocks.");
        PIPE_WALL_WAVELENGTH_FACTOR = decimal(b, "wallWavelengthFactor", d.pipe().wallWavelengthFactor(), 0.02, 2.0,
                "Size of the large wall bumps as a fraction of the pipe diameter.");
        PIPE_WALL_MIN_WAVELENGTH = decimal(b, "wallMinWavelength", d.pipe().wallMinWavelength(), 2.0, 200.0,
                "Smallest size of the large wall bumps in blocks.");
        PIPE_WALL_FINE_SHARE = decimal(b, "wallFineShare", d.pipe().wallFineShare(), 0.0, 1.0,
                "Share of fine roughness in the wall noise; 0 gives smooth bumps only.");
        PIPE_WALL_FINE_SCALE = decimal(b, "wallFineScale", d.pipe().wallFineScale(), 0.05, 1.0,
                "Size of the fine wall roughness relative to the large bumps.");
        PIPE_WALL_VERTICAL_STRETCH = decimal(b, "wallVerticalStretch", d.pipe().wallVerticalStretch(), 0.2, 10.0,
                "How much the wall noise is stretched vertically; higher gives smoother vertical walls.");
        b.pop();

        b.comment("Rock types inside pipes.").push("facies");
        FACIES_WEATHERING_DEPTH = decimal(b, "weatheringDepth", d.facies().weatheringDepth(), 0.0, 200.0,
                "Depth below the surface down to which kimberlite is weathered, in blocks.");
        FACIES_WEATHERING_NOISE = decimal(b, "weatheringNoise", d.facies().weatheringNoise(), 0.0, 50.0,
                "Variation of the weathering depth in blocks.");
        FACIES_CONTACT_FRACTION = decimal(b, "contactFraction", d.facies().contactFraction(), 0.0, 0.5,
                "Outer fraction of the radius that forms the contact breccia.");
        FACIES_SERPENTINE_BAND = decimal(b, "serpentinizationBand", d.facies().serpentinizationBand(), 0.0, 200.0,
                "Depth of the band below the weathering zone with more serpentinized rock, in blocks.");
        FACIES_SERPENTINE_THRESHOLD = decimal(b, "serpentinizationThreshold", d.facies().serpentinizationThreshold(),
                -1.0, 2.0, "Noise value above which rock is serpentinized at depth; higher means less.");
        FACIES_XENOLITH_THRESHOLD = decimal(b, "xenolithThreshold", d.facies().xenolithThreshold(), -1.0, 2.0,
                "Noise value above which a crustal xenolith replaces the rock; higher means fewer (0.56 gives 7%).");
        FACIES_PERIDOTITE_THRESHOLD = decimal(b, "peridotiteThreshold", d.facies().peridotiteThreshold(), -1.0, 2.0,
                "Noise value above which a mantle xenolith replaces the rock; higher means fewer (0.855 gives 0.4%).");
        FACIES_CONTACT_WAVELENGTH = decimal(b, "contactWavelength", d.facies().contactWavelength(), 1.0, 100.0,
                "Size of the host-rock fragments in the contact breccia, in blocks.");
        FACIES_XENOLITH_WAVELENGTH = decimal(b, "xenolithWavelength", d.facies().xenolithWavelength(), 1.0, 100.0,
                "Size of crustal xenoliths in blocks.");
        FACIES_PERIDOTITE_WAVELENGTH = decimal(b, "peridotiteWavelength", d.facies().peridotiteWavelength(), 1.0, 100.0,
                "Size of mantle xenoliths in blocks.");
        FACIES_SERPENTINE_WAVELENGTH = decimal(b, "serpentineWavelength", d.facies().serpentineWavelength(), 1.0, 100.0,
                "Size of serpentinized patches in blocks.");
        FACIES_WEATHERING_WAVELENGTH = decimal(b, "weatheringWavelength", d.facies().weatheringWavelength(), 1.0, 200.0,
                "Size of the bumps of the weathering depth in blocks.");
        FACIES_SERPENTINE_BAND_BONUS = decimal(b, "serpentineBandBonus", d.facies().serpentineBandBonus(), 0.0, 2.0,
                "Extra serpentinization in the band below the weathering zone.");
        FACIES_SERPENTINE_WALL_BONUS = decimal(b, "serpentineWallBonus", d.facies().serpentineWallBonus(), 0.0, 2.0,
                "Extra serpentinization near the pipe wall.");
        FACIES_SERPENTINE_WALL_START = decimal(b, "serpentineWallStart", d.facies().serpentineWallStart(), 0.0, 1.0,
                "Wall distance (0 axis, 1 wall) from which the wall bonus applies.");
        FACIES_CONTACT_THRESHOLD = decimal(b, "contactThreshold", d.facies().contactThreshold(), -1.0, 2.0,
                "Noise value above which a contact zone block stays host rock; higher means more breccia.");
        b.pop();

        b.comment("Crater bowl and lake.").push("crater");
        CRATER_DEPTH_PER_DIAMETER = decimal(b, "depthPerDiameter", d.crater().depthPerDiameter(), 0.0, 0.5,
                "Bowl depth as a fraction of the pipe diameter.");
        CRATER_MIN_DEPTH = integer(b, "minDepth", d.crater().minDepth(), 0, 64, "Smallest bowl depth in blocks.");
        CRATER_MAX_DEPTH = integer(b, "maxDepth", d.crater().maxDepth(), 0, 128, "Largest bowl depth in blocks.");
        CRATER_FLOOR_LAYERS = integer(b, "floorLayers", d.crater().floorLayers(), 0, 16,
                "Volcaniclastic layers at the bowl floor.");
        CRATER_RING_POINTS = integer(b, "ringPoints", d.crater().ringPoints(), 3, 64,
                "Terrain samples on the ring around a crater that set the rim height and lake level.");
        CRATER_MAX_WALL_HEIGHT = integer(b, "maxWallHeight", d.crater().maxWallHeight(), 0, 64,
                "Tallest wall the bowl may cut into sloping terrain; never less than the bowl depth. Lower values give gentler rims.");
        CRATER_SOIL_LAYERS = integer(b, "soilLayers", d.crater().soilLayers(), 0, 16,
                "Soil blocks laid on the bowl floor so the crater is overgrown; 0 leaves bare volcaniclastic rock.");
        CRATER_LAKE_IN_DRY_BIOMES = flag(b, "lakeInDryBiomes", d.crater().lakeInDryBiomes(),
                "Fill craters with water even in biomes without precipitation (desert, savanna, badlands).");
        b.pop();

        b.comment("Ore grade of pipes: barren, poor and rich pipes, with varying rock phases.").push("grade");
        GRADE_BARREN_SHARE = decimal(b, "barrenShare", d.grade().barrenShare(), 0.0, 1.0,
                "Share of pipes without graded ore (diamonds).");
        GRADE_POOR_SHARE = decimal(b, "poorShare", d.grade().poorShare(), 0.0, 1.0,
                "Share of pipes with a reduced ore share; the rest is rich.");
        GRADE_POOR_FACTOR = decimal(b, "poorFactor", d.grade().poorFactor(), 0.0, 1.0,
                "Factor applied to the ore share of poor pipes.");
        GRADE_PHASE_WAVELENGTH = decimal(b, "phaseWavelength", d.grade().phaseWavelength(), 4.0, 500.0,
                "Size of the rock phases in blocks; each has its own grade.");
        GRADE_PHASE_AMPLITUDE = decimal(b, "phaseAmplitude", d.grade().phaseAmplitude(), 0.0, 1.0,
                "How far the grade of a phase may deviate from the pipe grade.");
        b.pop();

        b.comment("Indicator mineral trails: grains washed downhill from a pipe, found by panning.").push("plume");
        PLUME_STEP_LENGTH = decimal(b, "stepLength", d.plume().stepLength(), 8.0, 128.0,
                "Distance in blocks between two points of the trail path.");
        PLUME_MAX_LENGTH = decimal(b, "maxLength", d.plume().maxLength(), 100.0, 5000.0,
                "Longest trail in blocks.");
        PLUME_INERTIA = decimal(b, "inertia", d.plume().inertia(), 0.0, 0.95,
                "Share of the previous heading kept at each step; higher values give straighter trails.");
        PLUME_FLAT_SLOPE = decimal(b, "flatSlope", d.plume().flatSlope(), 0.001, 1.0,
                "Terrain slope in blocks per block at which the trail follows the downhill direction fully.");
        PLUME_START_WIDTH = decimal(b, "startWidth", d.plume().startWidth(), 1.0, 200.0,
                "Half width of the trail at the pipe in blocks.");
        PLUME_WIDTH_GROWTH = decimal(b, "widthGrowth", d.plume().widthGrowth(), 0.0, 1.0,
                "Growth of the half width per block of trail length.");
        PLUME_COLD_FACTOR = decimal(b, "coldFactor", d.plume().coldFactor(), 0.0, 10.0,
                "Trail length factor for pipes in cold biomes; glaciers carry grains far.");
        PLUME_DRY_FACTOR = decimal(b, "dryFactor", d.plume().dryFactor(), 0.0, 10.0,
                "Trail length factor for pipes in dry biomes.");
        PLUME_OCEAN_FACTOR = decimal(b, "oceanFactor", d.plume().oceanFactor(), 0.0, 10.0,
                "Trail length factor for pipes in oceans; 0 leaves grains only at the pipe.");
        PLUME_MAX_SEA_DEPTH = decimal(b, "maxSeaDepth", d.plume().maxSeaDepth(), 0.0, 200.0,
                "Depth in blocks below the sea surface at which a trail ends; trails run on over shallow sea floor.");
        PYROPE_LENGTH = decimal(b, "pyropeLength", d.plume().pyrope().length(), 10.0, 5000.0,
                "Length in blocks over which pyrope garnet falls to 1/e.");
        PYROPE_PEAK = decimal(b, "pyropePeak", d.plume().pyrope().peak(), 0.0, 1.0,
                "Chance per pan of finding pyrope garnet next to the pipe.");
        CHROMITE_LENGTH = decimal(b, "chromiteLength", d.plume().chromite().length(), 10.0, 5000.0,
                "Length in blocks over which chromite falls to 1/e.");
        CHROMITE_PEAK = decimal(b, "chromitePeak", d.plume().chromite().peak(), 0.0, 1.0,
                "Chance per pan of finding chromite next to the pipe.");
        ILMENITE_LENGTH = decimal(b, "ilmeniteLength", d.plume().ilmenite().length(), 10.0, 5000.0,
                "Length in blocks over which ilmenite falls to 1/e; it is heavy and stops early.");
        ILMENITE_PEAK = decimal(b, "ilmenitePeak", d.plume().ilmenite().peak(), 0.0, 1.0,
                "Chance per pan of finding ilmenite next to the pipe.");
        b.pop();

        b.comment("Share of the blocks in the ore's rock zones that become ore.").push("minerals");
        Deposit pipe = OreConfig.registry().deposits().get(OreConfig.KIMBERLITE_PIPE);
        for (Mineral mineral : pipe.minerals()) {
            MINERAL_FRACTIONS.put(mineral.ore(), decimal(b, mineral.ore() + "Fraction", mineral.fraction(), 0.0, 1.0,
                    "Share of rock blocks in " + mineral.zones() + " that become " + mineral.ore() + " ore."));
        }
        b.pop();
        SPEC = b.build();
    }

    private KimberliteConfig() {
    }

    private static Supplier<Integer> integer(ModConfigSpec.Builder builder, String name, int value, int min, int max,
            String comment) {
        ModConfigSpec.IntValue config = builder.comment(comment).defineInRange(name, value, min, max);
        return config::get;
    }

    private static Supplier<Boolean> flag(ModConfigSpec.Builder builder, String name, boolean value, String comment) {
        ModConfigSpec.BooleanValue config = builder.comment(comment).define(name, value);
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
    public static KimberliteParams params() {
        return new KimberliteParams(
                new KimberliteParams.CratonParams(CRATON_WAVELENGTH.get(), CRATON_THRESHOLD.get()),
                new KimberliteParams.FieldParams(FIELD_CELL_SIZE.get(), FIELD_CHANCE.get(), FIELD_JITTER.get(),
                        FIELD_MIN_RADIUS.get(), FIELD_MAX_RADIUS.get(), FIELD_MIN_PIPES.get(), FIELD_MAX_PIPES.get(),
                        FIELD_MIN_SPACING.get(), FIELD_PLACEMENT_ATTEMPTS.get()),
                new KimberliteParams.PipeParams(PIPE_MEDIAN_DIAMETER.get(), PIPE_DIAMETER_SIGMA.get(),
                        PIPE_MIN_DIAMETER.get(), PIPE_MAX_DIAMETER.get(), PIPE_CRATER_BASE_Y.get(),
                        PIPE_CRATER_HEIGHT.get(), PIPE_CRATER_WIDENING.get(), PIPE_DIATREME_TAPER.get(),
                        PIPE_ROOT_Y.get(), PIPE_ROOT_MIN_RADIUS.get(), PIPE_BOTTOM_Y.get(), PIPE_WALL_NOISE.get(),
                        PIPE_MIN_ASPECT.get(), PIPE_OUTCROP_THRESHOLD.get(), PIPE_OUTCROP_WAVELENGTH.get(),
                        PIPE_WALL_WAVELENGTH_FACTOR.get(), PIPE_WALL_MIN_WAVELENGTH.get(),
                        PIPE_WALL_FINE_SHARE.get(), PIPE_WALL_FINE_SCALE.get(), PIPE_WALL_VERTICAL_STRETCH.get()),
                new KimberliteParams.FaciesParams(FACIES_WEATHERING_DEPTH.get(), FACIES_WEATHERING_NOISE.get(),
                        FACIES_CONTACT_FRACTION.get(), FACIES_SERPENTINE_BAND.get(),
                        FACIES_SERPENTINE_THRESHOLD.get(), FACIES_XENOLITH_THRESHOLD.get(),
                        FACIES_PERIDOTITE_THRESHOLD.get(), FACIES_CONTACT_WAVELENGTH.get(),
                        FACIES_XENOLITH_WAVELENGTH.get(), FACIES_PERIDOTITE_WAVELENGTH.get(),
                        FACIES_SERPENTINE_WAVELENGTH.get(), FACIES_WEATHERING_WAVELENGTH.get(),
                        FACIES_SERPENTINE_BAND_BONUS.get(), FACIES_SERPENTINE_WALL_BONUS.get(),
                        FACIES_SERPENTINE_WALL_START.get(), FACIES_CONTACT_THRESHOLD.get()),
                new KimberliteParams.CraterParams(CRATER_DEPTH_PER_DIAMETER.get(), CRATER_MIN_DEPTH.get(),
                        CRATER_MAX_DEPTH.get(), CRATER_FLOOR_LAYERS.get(), CRATER_RING_POINTS.get(),
                        CRATER_MAX_WALL_HEIGHT.get(), CRATER_SOIL_LAYERS.get(),
                        CRATER_LAKE_IN_DRY_BIOMES.get()),
                new KimberliteParams.GradeParams(GRADE_BARREN_SHARE.get(), GRADE_POOR_SHARE.get(),
                        GRADE_POOR_FACTOR.get(), GRADE_PHASE_WAVELENGTH.get(), GRADE_PHASE_AMPLITUDE.get()),
                new KimberliteParams.PlumeParams(PLUME_STEP_LENGTH.get(), PLUME_MAX_LENGTH.get(),
                        PLUME_INERTIA.get(), PLUME_FLAT_SLOPE.get(), PLUME_START_WIDTH.get(),
                        PLUME_WIDTH_GROWTH.get(), PLUME_COLD_FACTOR.get(), PLUME_DRY_FACTOR.get(),
                        PLUME_OCEAN_FACTOR.get(), PLUME_MAX_SEA_DEPTH.get(),
                        new KimberliteParams.IndicatorParams(PYROPE_LENGTH.get(), PYROPE_PEAK.get()),
                        new KimberliteParams.IndicatorParams(CHROMITE_LENGTH.get(), CHROMITE_PEAK.get()),
                        new KimberliteParams.IndicatorParams(ILMENITE_LENGTH.get(), ILMENITE_PEAK.get())))
                .sanitized();
    }

    /**
     * Reads the configured ore shares of the kimberlite pipe.
     *
     * @return ore share by ore id
     */
    public static Map<String, Double> fractions() {
        Map<String, Double> fractions = new HashMap<>();
        MINERAL_FRACTIONS.forEach((ore, value) -> fractions.put(ore, value.get()));
        return fractions;
    }
}
