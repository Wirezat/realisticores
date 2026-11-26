package de.wirezat.realisticores.kimberlite;

import static de.wirezat.realisticores.config.ConfigValues.decimal;
import static de.wirezat.realisticores.config.ConfigValues.flag;
import static de.wirezat.realisticores.config.ConfigValues.integer;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.config.MineralConfig;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Feature config of the kimberlite pipe: where, how big and how rich the pipes are. */
public final class KimberliteConfig {
    public static final ModConfigSpec SPEC;
    public static final MineralConfig MINERALS;

    private static final Supplier<KimberliteParams> PARAMS;

    static {
        KimberliteParams d = KimberliteParams.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Craton mask: only cratons (old, stable crust) hold kimberlite fields.").push("craton");
        Supplier<Double> cratonWavelength = decimal(b, "wavelength", d.craton().wavelength(), 100.0, 100000.0,
                "Size of craton regions in blocks; larger means fewer, bigger cratons.");
        Supplier<Double> cratonThreshold = decimal(b, "threshold", d.craton().threshold(), -1.0, 2.0,
                "Noise value above which a position is craton; higher means less craton (about 17% at 0.45).");
        b.pop();
        Supplier<KimberliteParams.CratonParams> craton = () -> new KimberliteParams.CratonParams(
                cratonWavelength.get(), cratonThreshold.get());

        b.comment("Kimberlite fields: clusters of pipes inside cratons.").push("field");
        Supplier<Integer> fieldCellSize = integer(b, "cellSize", d.field().cellSize(), 256, 100000,
                "Grid cell size in blocks; each cell holds at most one field.");
        Supplier<Double> fieldChance = decimal(b, "chance", d.field().chance(), 0.0, 1.0,
                "Probability that a cell on a craton holds a field.");
        Supplier<Double> fieldJitter = decimal(b, "jitter", d.field().jitter(), 0.0, 0.5,
                "Maximum offset of a field from its cell centre, as a fraction of the cell size.");
        Supplier<Integer> fieldMinRadius = integer(b, "minRadius", d.field().minRadius(), 16, 5000,
                "Smallest field radius in blocks.");
        Supplier<Integer> fieldMaxRadius = integer(b, "maxRadius", d.field().maxRadius(), 16, 5000,
                "Largest field radius in blocks.");
        Supplier<Integer> fieldMinPipes = integer(b, "minPipes", d.field().minPipes(), 1, 200,
                "Fewest pipes attempted per field.");
        Supplier<Integer> fieldMaxPipes = integer(b, "maxPipes", d.field().maxPipes(), 1, 200,
                "Most pipes attempted per field.");
        Supplier<Integer> fieldMinSpacing = integer(b, "minSpacing", d.field().minSpacing(), 1, 1000,
                "Minimum distance between pipe centres in a field, in blocks.");
        Supplier<Integer> fieldPlacementAttempts = integer(b, "placementAttempts", d.field().placementAttempts(), 1,
                100,
                "Tries to find a free position for each pipe before it is skipped; fewer tries give sparser fields.");
        b.pop();
        Supplier<KimberliteParams.FieldParams> field = () -> new KimberliteParams.FieldParams(
                fieldCellSize.get(), fieldChance.get(), fieldJitter.get(), fieldMinRadius.get(), fieldMaxRadius.get(),
                fieldMinPipes.get(), fieldMaxPipes.get(), fieldMinSpacing.get(), fieldPlacementAttempts.get());

        b.comment("Pipe size and shape.").push("pipe");
        Supplier<Integer> pipeMedianDiameter = integer(b, "medianDiameter", d.pipe().medianDiameter(), 8, 1000,
                "Median surface diameter in blocks.");
        Supplier<Double> pipeDiameterSigma = decimal(b, "diameterSigma", d.pipe().diameterSigma(), 0.0, 2.0,
                "Spread of diameters (log-normal); higher gives more very small and very large pipes.");
        Supplier<Integer> pipeMinDiameter = integer(b, "minDiameter", d.pipe().minDiameter(), 4, 1000,
                "Smallest diameter in blocks.");
        Supplier<Integer> pipeMaxDiameter = integer(b, "maxDiameter", d.pipe().maxDiameter(), 8, 2000,
                "Largest diameter in blocks.");
        Supplier<Integer> pipeCraterBaseY = integer(b, "craterBaseY", d.pipe().craterBaseY(), -64, 320,
                "Height where the crater flare starts.");
        Supplier<Integer> pipeCraterHeight = integer(b, "craterHeight", d.pipe().craterHeight(), 1, 200,
                "Height of the crater flare in blocks.");
        Supplier<Double> pipeCraterWidening = decimal(b, "craterWidening", d.pipe().craterWidening(), 1.0, 3.0,
                "Radius factor reached at the top of the crater flare.");
        Supplier<Double> pipeDiatremeTaper = decimal(b, "diatremeTaper", d.pipe().diatremeTaper(), 0.0, 0.9,
                "Fraction of the radius lost between the crater base and the root.");
        Supplier<Integer> pipeRootY = integer(b, "rootY", d.pipe().rootY(), -64, 320,
                "Height where the diatreme turns into the root.");
        Supplier<Double> pipeRootMinRadius = decimal(b, "rootMinRadius", d.pipe().rootMinRadius(), 0.5, 50.0,
                "Radius in blocks reached at the bottom of the pipe.");
        Supplier<Integer> pipeBottomY = integer(b, "bottomY", d.pipe().bottomY(), -64, 320,
                "Lowest height of the pipe.");
        Supplier<Double> pipeWallNoise = decimal(b, "wallNoiseStrength", d.pipe().wallNoiseStrength(), 0.0, 0.8,
                "How strongly the wall deviates from a clean outline, as a fraction of the radius.");
        Supplier<Double> pipeMinAspect = decimal(b, "minAspect", d.pipe().minAspect(), 0.2, 1.0,
                "Smallest ratio of short to long axis of the outline; 1 means round pipes.");
        Supplier<Double> pipeOutcropThreshold = decimal(b, "outcropThreshold", d.pipe().outcropThreshold(), -1.0, 2.0,
                "Noise value above which kimberlite shows at the surface; higher means fewer outcrops.");
        Supplier<Double> pipeOutcropWavelength = decimal(b, "outcropWavelength", d.pipe().outcropWavelength(), 2.0,
                200.0, "Size of outcrop patches in blocks.");
        Supplier<Double> pipeWallWavelengthFactor = decimal(b, "wallWavelengthFactor", d.pipe().wallWavelengthFactor(),
                0.02, 2.0, "Size of the large wall bumps as a fraction of the pipe diameter.");
        Supplier<Double> pipeWallMinWavelength = decimal(b, "wallMinWavelength", d.pipe().wallMinWavelength(), 2.0,
                200.0, "Smallest size of the large wall bumps in blocks.");
        Supplier<Double> pipeWallFineShare = decimal(b, "wallFineShare", d.pipe().wallFineShare(), 0.0, 1.0,
                "Share of fine roughness in the wall noise; 0 gives smooth bumps only.");
        Supplier<Double> pipeWallFineScale = decimal(b, "wallFineScale", d.pipe().wallFineScale(), 0.05, 1.0,
                "Size of the fine wall roughness relative to the large bumps.");
        Supplier<Double> pipeWallVerticalStretch = decimal(b, "wallVerticalStretch", d.pipe().wallVerticalStretch(),
                0.2, 10.0, "How much the wall noise is stretched vertically; higher gives smoother vertical walls.");
        b.pop();
        Supplier<KimberliteParams.PipeParams> pipe = () -> new KimberliteParams.PipeParams(
                pipeMedianDiameter.get(), pipeDiameterSigma.get(), pipeMinDiameter.get(), pipeMaxDiameter.get(),
                pipeCraterBaseY.get(), pipeCraterHeight.get(), pipeCraterWidening.get(), pipeDiatremeTaper.get(),
                pipeRootY.get(), pipeRootMinRadius.get(), pipeBottomY.get(), pipeWallNoise.get(), pipeMinAspect.get(),
                pipeOutcropThreshold.get(), pipeOutcropWavelength.get(), pipeWallWavelengthFactor.get(),
                pipeWallMinWavelength.get(), pipeWallFineShare.get(), pipeWallFineScale.get(),
                pipeWallVerticalStretch.get());

        b.comment("Rock types inside pipes.").push("facies");
        Supplier<Double> faciesWeatheringDepth = decimal(b, "weatheringDepth", d.facies().weatheringDepth(), 0.0, 200.0,
                "Depth below the surface down to which kimberlite is weathered, in blocks.");
        Supplier<Double> faciesWeatheringNoise = decimal(b, "weatheringNoise", d.facies().weatheringNoise(), 0.0, 50.0,
                "Variation of the weathering depth in blocks.");
        Supplier<Double> faciesContactFraction = decimal(b, "contactFraction", d.facies().contactFraction(), 0.0, 0.5,
                "Outer fraction of the radius that forms the contact breccia.");
        Supplier<Double> faciesSerpentineBand = decimal(b, "serpentinizationBand", d.facies().serpentinizationBand(),
                0.0, 200.0, "Depth of the band below the weathering zone with more serpentinized rock, in blocks.");
        Supplier<Double> faciesSerpentineThreshold = decimal(b, "serpentinizationThreshold",
                d.facies().serpentinizationThreshold(), -1.0, 2.0,
                "Noise value above which rock is serpentinized at depth; higher means less.");
        Supplier<Double> faciesXenolithThreshold = decimal(b, "xenolithThreshold", d.facies().xenolithThreshold(), -1.0,
                2.0,
                "Noise value above which a crustal xenolith replaces the rock; higher means fewer (0.56 gives 7%).");
        Supplier<Double> faciesPeridotiteThreshold = decimal(b, "peridotiteThreshold", d.facies().peridotiteThreshold(),
                -1.0, 2.0,
                "Noise value above which a mantle xenolith replaces the rock; higher means fewer (0.855 gives 0.4%).");
        Supplier<Double> faciesContactWavelength = decimal(b, "contactWavelength", d.facies().contactWavelength(), 1.0,
                100.0, "Size of the host-rock fragments in the contact breccia, in blocks.");
        Supplier<Double> faciesXenolithWavelength = decimal(b, "xenolithWavelength", d.facies().xenolithWavelength(),
                1.0, 100.0, "Size of crustal xenoliths in blocks.");
        Supplier<Double> faciesPeridotiteWavelength = decimal(b, "peridotiteWavelength",
                d.facies().peridotiteWavelength(), 1.0, 100.0, "Size of mantle xenoliths in blocks.");
        Supplier<Double> faciesSerpentineWavelength = decimal(b, "serpentineWavelength",
                d.facies().serpentineWavelength(), 1.0, 100.0, "Size of serpentinized patches in blocks.");
        Supplier<Double> faciesWeatheringWavelength = decimal(b, "weatheringWavelength",
                d.facies().weatheringWavelength(), 1.0, 200.0, "Size of the bumps of the weathering depth in blocks.");
        Supplier<Double> faciesSerpentineBandBonus = decimal(b, "serpentineBandBonus", d.facies().serpentineBandBonus(),
                0.0, 2.0, "Extra serpentinization in the band below the weathering zone.");
        Supplier<Double> faciesSerpentineWallBonus = decimal(b, "serpentineWallBonus", d.facies().serpentineWallBonus(),
                0.0, 2.0, "Extra serpentinization near the pipe wall.");
        Supplier<Double> faciesSerpentineWallStart = decimal(b, "serpentineWallStart", d.facies().serpentineWallStart(),
                0.0, 1.0, "Wall distance (0 axis, 1 wall) from which the wall bonus applies.");
        Supplier<Double> faciesContactThreshold = decimal(b, "contactThreshold", d.facies().contactThreshold(), -1.0,
                2.0, "Noise value above which a contact zone block stays host rock; higher means more breccia.");
        b.pop();
        Supplier<KimberliteParams.FaciesParams> facies = () -> new KimberliteParams.FaciesParams(
                faciesWeatheringDepth.get(), faciesWeatheringNoise.get(), faciesContactFraction.get(),
                faciesSerpentineBand.get(), faciesSerpentineThreshold.get(), faciesXenolithThreshold.get(),
                faciesPeridotiteThreshold.get(), faciesContactWavelength.get(), faciesXenolithWavelength.get(),
                faciesPeridotiteWavelength.get(), faciesSerpentineWavelength.get(), faciesWeatheringWavelength.get(),
                faciesSerpentineBandBonus.get(), faciesSerpentineWallBonus.get(), faciesSerpentineWallStart.get(),
                faciesContactThreshold.get());

        b.comment("Crater bowl and lake.").push("crater");
        Supplier<Double> craterDepthPerDiameter = decimal(b, "depthPerDiameter", d.crater().depthPerDiameter(), 0.0,
                0.5, "Bowl depth as a fraction of the pipe diameter.");
        Supplier<Integer> craterMinDepth = integer(b, "minDepth", d.crater().minDepth(), 0, 64,
                "Smallest bowl depth in blocks.");
        Supplier<Integer> craterMaxDepth = integer(b, "maxDepth", d.crater().maxDepth(), 0, 128,
                "Largest bowl depth in blocks.");
        Supplier<Integer> craterFloorLayers = integer(b, "floorLayers", d.crater().floorLayers(), 0, 16,
                "Volcaniclastic layers at the bowl floor.");
        Supplier<Integer> craterRingPoints = integer(b, "ringPoints", d.crater().ringPoints(), 3, 64,
                "Terrain samples on the ring around a crater that set the rim height and lake level.");
        Supplier<Integer> craterMaxWallHeight = integer(b, "maxWallHeight", d.crater().maxWallHeight(), 0, 64,
                "Tallest wall the bowl may cut into sloping terrain; never less than the bowl depth." + " Lower values"
                        + " give gentler rims.");
        Supplier<Integer> craterSoilLayers = integer(b, "soilLayers", d.crater().soilLayers(), 0, 16,
                "Soil blocks laid on the bowl floor so the crater is overgrown; 0 leaves bare volcaniclastic rock.");
        Supplier<Boolean> craterLakeInDryBiomes = flag(b, "lakeInDryBiomes", d.crater().lakeInDryBiomes(),
                "Fill craters with water even in biomes without precipitation (desert, savanna, badlands).");
        b.pop();
        Supplier<KimberliteParams.CraterParams> crater = () -> new KimberliteParams.CraterParams(
                craterDepthPerDiameter.get(), craterMinDepth.get(), craterMaxDepth.get(), craterFloorLayers.get(),
                craterRingPoints.get(), craterMaxWallHeight.get(), craterSoilLayers.get(), craterLakeInDryBiomes.get());

        b.comment("Ore grade of pipes: barren, poor and rich pipes, with varying rock phases.").push("grade");
        Supplier<Double> gradeBarrenShare = decimal(b, "barrenShare", d.grade().barrenShare(), 0.0, 1.0,
                "Share of pipes without graded ore (diamonds).");
        Supplier<Double> gradePoorShare = decimal(b, "poorShare", d.grade().poorShare(), 0.0, 1.0,
                "Share of pipes with a reduced ore share; the rest is rich.");
        Supplier<Double> gradePoorFactor = decimal(b, "poorFactor", d.grade().poorFactor(), 0.0, 1.0,
                "Factor applied to the ore share of poor pipes.");
        Supplier<Double> gradePhaseWavelength = decimal(b, "phaseWavelength", d.grade().phaseWavelength(), 4.0, 500.0,
                "Size of the rock phases in blocks; each has its own grade.");
        Supplier<Double> gradePhaseAmplitude = decimal(b, "phaseAmplitude", d.grade().phaseAmplitude(), 0.0, 1.0,
                "How far the grade of a phase may deviate from the pipe grade.");
        b.pop();
        Supplier<KimberliteParams.GradeParams> grade = () -> new KimberliteParams.GradeParams(
                gradeBarrenShare.get(), gradePoorShare.get(), gradePoorFactor.get(), gradePhaseWavelength.get(),
                gradePhaseAmplitude.get());

        b.comment("Indicator mineral trails: grains washed downhill from a pipe, found by panning.").push("plume");
        Supplier<Double> plumeStepLength = decimal(b, "stepLength", d.plume().stepLength(), 8.0, 128.0,
                "Distance in blocks between two points of the trail path.");
        Supplier<Double> plumeMaxLength = decimal(b, "maxLength", d.plume().maxLength(), 100.0, 5000.0,
                "Longest trail in blocks.");
        Supplier<Double> plumeInertia = decimal(b, "inertia", d.plume().inertia(), 0.0, 0.95,
                "Share of the previous heading kept at each step; higher values give straighter trails.");
        Supplier<Double> plumeFlatSlope = decimal(b, "flatSlope", d.plume().flatSlope(), 0.001, 1.0,
                "Terrain slope in blocks per block at which the trail follows the downhill direction fully.");
        Supplier<Double> plumeStartWidth = decimal(b, "startWidth", d.plume().startWidth(), 1.0, 200.0,
                "Half width of the trail at the pipe in blocks.");
        Supplier<Double> plumeWidthGrowth = decimal(b, "widthGrowth", d.plume().widthGrowth(), 0.0, 1.0,
                "Growth of the half width per block of trail length.");
        Supplier<Double> plumeColdFactor = decimal(b, "coldFactor", d.plume().coldFactor(), 0.0, 10.0,
                "Trail length factor for pipes in cold biomes; glaciers carry grains far.");
        Supplier<Double> plumeDryFactor = decimal(b, "dryFactor", d.plume().dryFactor(), 0.0, 10.0,
                "Trail length factor for pipes in dry biomes.");
        Supplier<Double> plumeOceanFactor = decimal(b, "oceanFactor", d.plume().oceanFactor(), 0.0, 10.0,
                "Trail length factor for pipes in oceans; 0 leaves grains only at the pipe.");
        Supplier<Double> plumeMaxSeaDepth = decimal(b, "maxSeaDepth", d.plume().maxSeaDepth(), 0.0, 200.0,
                "Depth in blocks below the sea surface at which a trail ends; trails run on over shallow sea floor.");
        Supplier<Double> pyropeLength = decimal(b, "pyropeLength", d.plume().pyrope().length(), 10.0, 5000.0,
                "Length in blocks over which pyrope garnet falls to 1/e.");
        Supplier<Double> pyropePeak = decimal(b, "pyropePeak", d.plume().pyrope().peak(), 0.0, 1.0,
                "Measured share of pyrope garnet next to the pipe; 1 is 100 g per wash.");
        Supplier<Double> chromiteLength = decimal(b, "chromiteLength", d.plume().chromite().length(), 10.0, 5000.0,
                "Length in blocks over which chromite falls to 1/e.");
        Supplier<Double> chromitePeak = decimal(b, "chromitePeak", d.plume().chromite().peak(), 0.0, 1.0,
                "Measured share of chromite next to the pipe; 1 is 100 g per wash.");
        Supplier<Double> ilmeniteLength = decimal(b, "ilmeniteLength", d.plume().ilmenite().length(), 10.0, 5000.0,
                "Length in blocks over which ilmenite falls to 1/e; it is heavy and stops early.");
        Supplier<Double> ilmenitePeak = decimal(b, "ilmenitePeak", d.plume().ilmenite().peak(), 0.0, 1.0,
                "Measured share of ilmenite next to the pipe; 1 is 100 g per wash.");
        b.pop();
        Supplier<KimberliteParams.PlumeParams> plume = () -> new KimberliteParams.PlumeParams(
                plumeStepLength.get(), plumeMaxLength.get(), plumeInertia.get(), plumeFlatSlope.get(),
                plumeStartWidth.get(), plumeWidthGrowth.get(), plumeColdFactor.get(), plumeDryFactor.get(),
                plumeOceanFactor.get(), plumeMaxSeaDepth.get(),
                new KimberliteParams.IndicatorParams(pyropeLength.get(), pyropePeak.get()),
                new KimberliteParams.IndicatorParams(chromiteLength.get(), chromitePeak.get()),
                new KimberliteParams.IndicatorParams(ilmeniteLength.get(), ilmenitePeak.get()));

        MINERALS = MineralConfig.define(b, OreConfig.registry().deposits().get(KimberlitePipe.ID),
                mineral -> true, Map.of());
        PARAMS = () -> new KimberliteParams(craton.get(), field.get(), pipe.get(), facies.get(), crater.get(),
                grade.get(), plume.get()).sanitized();
        SPEC = b.build();
    }

    private KimberliteConfig() {
    }

    /**
     * Reads the current generation parameters from the config.
     *
     * @return parameters with contradicting values repaired
     */
    public static KimberliteParams params() {
        return PARAMS.get();
    }
}
