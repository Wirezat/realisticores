package de.wirezat.realisticores.coal;

import static de.wirezat.realisticores.config.ConfigValues.decimal;
import static de.wirezat.realisticores.config.ConfigValues.integer;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.worldgen.DoubleRange;
import de.wirezat.realisticores.worldgen.IntRange;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Feature config of the coal basin: where basins lie and what their seams look like. */
public final class CoalConfig {
    public static final ModConfigSpec SPEC;
    public static final MineralConfig MINERALS;

    private static final Supplier<CoalParams> PARAMS;
    private static final Map<String, String> DEFAULT_REPLACEMENTS = Map.of("coal", "realisticores:black_shale",
            "lignite", "realisticores:mudstone", "anthracite", "realisticores:black_shale");

    static {
        CoalParams d = CoalParams.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Position, size and outline of basins.").push("basin");
        Supplier<Integer> basinCellSize = integer(b, "cellSize", d.basin().cellSize(), 256, 100000,
                "Grid cell size in blocks; each cell holds at most one basin.");
        Supplier<Double> basinChance = decimal(b, "chance", d.basin().chance(), 0.0, 1.0,
                "Probability that a cell holds a basin.");
        Supplier<Double> basinJitter = decimal(b, "jitter", d.basin().jitter(), 0.0, 0.4,
                "Maximum offset of the centre from the cell centre, as a fraction of the cell size.");
        Supplier<Integer> basinMinRadius = integer(b, "minRadius", d.basin().radius().min(), 16, 20000,
                "Smallest mean radius in blocks.");
        Supplier<Integer> basinMaxRadius = integer(b, "maxRadius", d.basin().radius().max(), 16, 20000,
                "Largest mean radius in blocks.");
        Supplier<Double> basinMinAspect = decimal(b, "minAspect", d.basin().minAspect(), 0.2, 1.0,
                "Smallest ratio of short to long axis.");
        Supplier<Double> basinOutlineNoise = decimal(b, "outlineNoise", d.basin().outlineNoise(), 0.0, 0.5,
                "Fraction of the radius the outline may deviate by noise.");
        Supplier<Double> basinOutlineWavelength = decimal(b, "outlineWavelength", d.basin().outlineWavelength(), 1.0,
                5000.0, "Size of the outline bumps in blocks.");
        Supplier<Double> basinHardCoalShare = decimal(b, "hardCoalShare", d.basin().hardCoalShare(), 0.0, 1.0,
                "Share of basins with hard coal, the rest holds lignite.");
        b.pop();
        Supplier<CoalParams.BasinParams> basin = () -> new CoalParams.BasinParams(basinCellSize.get(),
                basinChance.get(), basinJitter.get(), new IntRange(basinMinRadius.get(), basinMaxRadius.get()),
                basinMinAspect.get(), basinOutlineNoise.get(), basinOutlineWavelength.get(), basinHardCoalShare.get());

        b.comment("Terrain limits; applied per basin, never per column.").push("relief");
        Supplier<Integer> reliefMountainRelief = integer(b, "mountainRelief", d.relief().mountainRelief(), 0, 2000,
                "Height difference in blocks inside a basin above which there is no basin.");
        Supplier<Integer> reliefFoothillRise = integer(b, "foothillRise", d.relief().foothillRise(), 0, 2000,
                "Height in blocks above the basin from which a rim point counts as mountain.");
        Supplier<Integer> reliefFoothillPoints = integer(b, "foothillPoints", d.relief().foothillPoints(), 1, 9,
                "Number of mountain points on the rim that make a basin folded.");
        b.pop();
        Supplier<CoalParams.ReliefParams> relief = () -> new CoalParams.ReliefParams(reliefMountainRelief.get(),
                reliefFoothillRise.get(), reliefFoothillPoints.get());

        Supplier<CoalParams.TypeParams> hardCoal = typeSection(b, "hard_coal", "Hard coal basins.", d.hardCoal());
        Supplier<CoalParams.TypeParams> lignite = typeSection(b, "lignite", "Lignite basins.", d.lignite());
        Supplier<CoalParams.TypeParams> folded = typeSection(b, "folded",
                "Folded basins at the foot of mountains.", d.folded());

        b.comment("Folds and anthracite core of folded basins.").push("fold");
        Supplier<Double> foldMinAmplitude = decimal(b, "minAmplitude", d.fold().amplitude().min(), 0.0, 200.0,
                "Smallest fold height in blocks.");
        Supplier<Double> foldMaxAmplitude = decimal(b, "maxAmplitude", d.fold().amplitude().max(), 0.0, 200.0,
                "Largest fold height in blocks.");
        Supplier<Double> foldMinWavelength = decimal(b, "minWavelength", d.fold().wavelength().min(), 1.0, 5000.0,
                "Smallest distance between two folds in blocks.");
        Supplier<Double> foldMaxWavelength = decimal(b, "maxWavelength", d.fold().wavelength().max(), 1.0, 5000.0,
                "Largest distance between two folds in blocks.");
        Supplier<Double> foldCoreShare = decimal(b, "coreShare", d.fold().coreShare(), 0.0, 1.0,
                "Part of the basin radius that holds anthracite.");
        b.pop();
        Supplier<CoalParams.FoldParams> fold = () -> new CoalParams.FoldParams(
                new DoubleRange(foldMinAmplitude.get(), foldMaxAmplitude.get()),
                new DoubleRange(foldMinWavelength.get(), foldMaxWavelength.get()), foldCoreShare.get());

        b.comment("Shape of the seams.").push("seam");
        Supplier<Double> seamCover = decimal(b, "cover", d.seam().cover(), 0.0, 100.0,
                "Blocks between the topmost seam and the basin datum at the centre.");
        Supplier<Double> seamThicknessBase = decimal(b, "thicknessBase", d.seam().thicknessBase(), 0.0, 5.0,
                "Constant part of the thickness factor.");
        Supplier<Double> seamThicknessNoise = decimal(b, "thicknessNoise", d.seam().thicknessNoise(), 0.0, 5.0,
                "Noise part of the thickness factor.");
        Supplier<Double> seamThicknessWavelength = decimal(b, "thicknessWavelength", d.seam().thicknessWavelength(),
                1.0, 5000.0, "Size of thick and thin zones in blocks.");
        Supplier<Double> seamPinchOut = decimal(b, "pinchOut", d.seam().pinchOut(), 0.01, 1.0,
                "Outer fraction of the basin in which seams thin out.");
        Supplier<Double> seamMinThickness = decimal(b, "minThickness", d.seam().minThickness(), 0.0, 200.0,
                "Thickness in blocks below which there is no seam.");
        Supplier<Double> seamWaveAmplitude = decimal(b, "waveAmplitude", d.seam().waveAmplitude(), 0.0, 100.0,
                "Height of the gentle waves in blocks.");
        Supplier<Double> seamWaveWavelength = decimal(b, "waveWavelength", d.seam().waveWavelength(), 1.0, 5000.0,
                "Size of the gentle waves in blocks.");
        Supplier<Double> seamSplitThreshold = decimal(b, "splitThreshold", d.seam().splitThreshold(), -1.0, 2.0,
                "Noise value (-1..1) above which a seam splits.");
        Supplier<Double> seamSplitWavelength = decimal(b, "splitWavelength", d.seam().splitWavelength(), 1.0, 5000.0,
                "Size of split zones in blocks.");
        Supplier<Integer> seamMaxParting = integer(b, "maxParting", d.seam().maxParting(), 1, 20,
                "Thickest parting between two benches in blocks.");
        Supplier<Double> seamMinThrow = decimal(b, "minThrow", d.seam().faultThrow().min(), 0.0, 200.0,
                "Smallest shift at a fault in blocks.");
        Supplier<Double> seamMaxThrow = decimal(b, "maxThrow", d.seam().faultThrow().max(), 0.0, 200.0,
                "Largest shift at a fault in blocks.");
        Supplier<Double> seamFaultWaveAmplitude = decimal(b, "faultWaveAmplitude", d.seam().faultWaveAmplitude(), 0.0,
                200.0, "How far a fault line bends in blocks.");
        Supplier<Double> seamFaultWaveWavelength = decimal(b, "faultWaveWavelength", d.seam().faultWaveWavelength(),
                1.0, 5000.0, "Size of the bends in blocks.");
        Supplier<Double> seamOutcropThreshold = decimal(b, "outcropThreshold", d.seam().outcropThreshold(), -1.0, 2.0,
                "Noise value (-1..1) above which a seam shows through the soil.");
        Supplier<Double> seamOutcropWavelength = decimal(b, "outcropWavelength", d.seam().outcropWavelength(), 1.0,
                5000.0, "Size of outcrops in blocks.");
        b.pop();
        Supplier<CoalParams.SeamParams> seam = () -> new CoalParams.SeamParams(seamCover.get(), seamThicknessBase.get(),
                seamThicknessNoise.get(), seamThicknessWavelength.get(), seamPinchOut.get(), seamMinThickness.get(),
                seamWaveAmplitude.get(), seamWaveWavelength.get(), seamSplitThreshold.get(), seamSplitWavelength.get(),
                seamMaxParting.get(), new DoubleRange(seamMinThrow.get(), seamMaxThrow.get()),
                seamFaultWaveAmplitude.get(), seamFaultWaveWavelength.get(), seamOutcropThreshold.get(),
                seamOutcropWavelength.get());

        b.comment("Rocks around a seam.").push("rock");
        Supplier<Integer> rockMinShale = integer(b, "minShale", d.rock().shale().min(), 0, 200,
                "Thinnest shale in blocks.");
        Supplier<Integer> rockMaxShale = integer(b, "maxShale", d.rock().shale().max(), 0, 200,
                "Thickest shale in blocks.");
        Supplier<Integer> rockMinBlackShale = integer(b, "minBlackShale", d.rock().blackShale().min(), 0, 200,
                "Thinnest black shale above a seam in blocks.");
        Supplier<Integer> rockMaxBlackShale = integer(b, "maxBlackShale", d.rock().blackShale().max(), 0, 200,
                "Thickest black shale above a seam in blocks.");
        Supplier<Integer> rockMinFireclay = integer(b, "minFireclay", d.rock().fireclay().min(), 0, 200,
                "Thinnest fireclay below a seam in blocks.");
        Supplier<Integer> rockMaxFireclay = integer(b, "maxFireclay", d.rock().fireclay().max(), 0, 200,
                "Thickest fireclay below a seam in blocks.");
        Supplier<Integer> rockMinRoofMudstone = integer(b, "minRoofMudstone", d.rock().roofMudstone().min(), 0, 200,
                "Thinnest mudstone above lignite in blocks.");
        Supplier<Integer> rockMaxRoofMudstone = integer(b, "maxRoofMudstone", d.rock().roofMudstone().max(), 0, 200,
                "Thickest mudstone above lignite in blocks.");
        Supplier<Integer> rockMinFloorMudstone = integer(b, "minFloorMudstone", d.rock().floorMudstone().min(), 0, 200,
                "Thinnest mudstone below lignite in blocks.");
        Supplier<Integer> rockMaxFloorMudstone = integer(b, "maxFloorMudstone", d.rock().floorMudstone().max(), 0, 200,
                "Thickest mudstone below lignite in blocks.");
        Supplier<Double> rockWavelength = decimal(b, "wavelength", d.rock().wavelength(), 1.0, 5000.0,
                "Size of thick and thin zones in blocks.");
        b.pop();
        Supplier<CoalParams.RockParams> rock = () -> new CoalParams.RockParams(
                new IntRange(rockMinShale.get(), rockMaxShale.get()),
                new IntRange(rockMinBlackShale.get(), rockMaxBlackShale.get()),
                new IntRange(rockMinFireclay.get(), rockMaxFireclay.get()),
                new IntRange(rockMinRoofMudstone.get(), rockMaxRoofMudstone.get()),
                new IntRange(rockMinFloorMudstone.get(), rockMaxFloorMudstone.get()), rockWavelength.get());

        b.comment("Loose coal and coaly soil above a seam.").push("surface");
        Supplier<Double> surfaceLooseDensity = decimal(b, "looseDensity", d.surface().looseDensity(), 0.0, 1.0,
                "Share of outcrop columns with loose coal; 0 turns it off.");
        Supplier<Integer> surfaceLooseMaxHeight = integer(b, "looseMaxHeight", d.surface().looseMaxHeight(), 0, 200,
                "Height in blocks of an eroded seam above the ground up to which its coal lies around.");
        Supplier<Double> surfaceSoilThreshold = decimal(b, "soilThreshold", d.surface().soilThreshold(), -1.0, 1.0,
                "Noise value (-1..1) above which the soil of an outcrop is coaly; 1 turns it off.");
        Supplier<Double> surfaceSoilWavelength = decimal(b, "soilWavelength", d.surface().soilWavelength(), 1.0, 5000.0,
                "Size of coaly patches in blocks.");
        Supplier<Integer> surfaceSoilFringe = integer(b, "soilFringe", d.surface().soilFringe(), 0, 200,
                "Height in blocks of an eroded seam above the ground up to which coaly soil frays out.");
        b.pop();
        Supplier<CoalParams.SurfaceParams> surface = () -> new CoalParams.SurfaceParams(surfaceLooseDensity.get(),
                surfaceLooseMaxHeight.get(), surfaceSoilThreshold.get(), surfaceSoilWavelength.get(),
                surfaceSoilFringe.get());

        b.comment("Sandstone with its roll front and the seam fires.").push("accessories");
        Supplier<Integer> accSandMin = integer(b, "sandMin", d.accessories().sand().min(), 0, 200,
                "Thinnest sandstone above a hard coal seam in blocks.");
        Supplier<Integer> accSandMax = integer(b, "sandMax", d.accessories().sand().max(), 0, 200,
                "Thickest sandstone above a hard coal seam in blocks.");
        Supplier<Integer> accLigniteSandMin = integer(b, "ligniteSandMin", d.accessories().ligniteSand().min(), 0, 200,
                "Thinnest sandstone above a lignite seam in blocks.");
        Supplier<Integer> accLigniteSandMax = integer(b, "ligniteSandMax", d.accessories().ligniteSand().max(), 0, 200,
                "Thickest sandstone above a lignite seam in blocks.");
        Supplier<Double> accSandCover = decimal(b, "sandCover", d.accessories().sandCover(), 0.0, 1.0,
                "Share of the area with sandstone.");
        Supplier<Double> accSandWavelength = decimal(b, "sandWavelength", d.accessories().sandWavelength(), 1.0, 5000.0,
                "Size of sandstone lenses in blocks.");
        Supplier<Double> accFrontMin = decimal(b, "frontMin", d.accessories().front().min(), 0.0, 1.0,
                "Innermost position of the roll front: distance to the basin centre,"
                        + " 0 in the centre and 1 at the rim.");
        Supplier<Double> accFrontMax = decimal(b, "frontMax", d.accessories().front().max(), 0.0, 1.0,
                "Outermost position of the roll front: distance to the basin centre,"
                        + " 0 in the centre and 1 at the rim.");
        Supplier<Integer> accFrontBand = integer(b, "frontBand", d.accessories().frontBand(), 1, 200,
                "Width in blocks of the uranium band inside the roll front.");
        Supplier<Integer> accFireCell = integer(b, "fireCell", d.accessories().fireCell(), 16, 10000,
                "Grid cell size of seam fires in blocks.");
        Supplier<Double> accFireChance = decimal(b, "fireChance", d.accessories().fireChance(), 0.0, 1.0,
                "Probability that a cell holds a fire; 0 turns seam fires off.");
        Supplier<Integer> accFireMinRadius = integer(b, "fireMinRadius", d.accessories().fireRadius().min(), 1, 8,
                "Smallest fire radius in blocks.");
        Supplier<Integer> accFireMaxRadius = integer(b, "fireMaxRadius", d.accessories().fireRadius().max(), 1, 8,
                "Largest fire radius in blocks.");
        Supplier<Integer> accFireDepth = integer(b, "fireDepth", d.accessories().fireDepth(), 1, 32,
                "Blocks below the surface that burn.");
        Supplier<Double> accBlackbandCover = decimal(b, "blackbandCover", d.accessories().blackbandCover(), 0.0, 1.0,
                "Share of the area where blackband ironstone lies on the topmost hard coal seam.");
        Supplier<Integer> accRustDepth = integer(b, "rustDepth", d.accessories().rustDepth(), 0, 16,
                "Blocks of shale below the soil that are rust-stained; 0 turns it off.");
        b.pop();
        Supplier<CoalParams.AccessoryParams> accessories = () -> new CoalParams.AccessoryParams(
                new IntRange(accSandMin.get(), accSandMax.get()),
                new IntRange(accLigniteSandMin.get(), accLigniteSandMax.get()), accSandCover.get(),
                accSandWavelength.get(), new DoubleRange(accFrontMin.get(), accFrontMax.get()), accFrontBand.get(),
                accFireCell.get(), accFireChance.get(), new IntRange(accFireMinRadius.get(), accFireMaxRadius.get()),
                accFireDepth.get(), accBlackbandCover.get(), accRustDepth.get());

        MINERALS = MineralConfig.define(b, OreConfig.registry().deposits().get(CoalBasin.ID),
                mineral -> CoalOres.isAccessory(mineral.zones()), DEFAULT_REPLACEMENTS);
        PARAMS = () -> new CoalParams(basin.get(), relief.get(), hardCoal.get(), lignite.get(), folded.get(),
                fold.get(), seam.get(), rock.get(), surface.get(), accessories.get()).sanitized();
        SPEC = b.build();
    }

    private CoalConfig() {
    }

    /**
     * Defines the section of one basin type; all types share the same keys.
     *
     * @param b builder at the top level of the config
     * @param name section name
     * @param comment explanation written above the section
     * @param d default values of the type
     * @return reader of the current values of the type
     */
    private static Supplier<CoalParams.TypeParams> typeSection(ModConfigSpec.Builder b, String name, String comment,
            CoalParams.TypeParams d) {
        b.comment(comment).push(name);
        Supplier<Integer> minDepth = integer(b, "minDepth", d.depth().min(), 1, 300,
                "Smallest depth of the bowl in blocks.");
        Supplier<Integer> maxDepth = integer(b, "maxDepth", d.depth().max(), 1, 300,
                "Largest depth of the bowl in blocks.");
        Supplier<Integer> minSeams = integer(b, "minSeams", d.seams().min(), 1, 10, "Fewest seams.");
        Supplier<Integer> maxSeams = integer(b, "maxSeams", d.seams().max(), 1, 10, "Most seams.");
        Supplier<Double> minThickness = decimal(b, "minThickness", d.thickness().min(), 0.0, 200.0,
                "Smallest seam thickness in blocks.");
        Supplier<Double> maxThickness = decimal(b, "maxThickness", d.thickness().max(), 0.0, 200.0,
                "Largest seam thickness in blocks.");
        Supplier<Integer> minSpacing = integer(b, "minSpacing", d.spacing().min(), 0, 200,
                "Smallest gap between two seams in blocks.");
        Supplier<Integer> maxSpacing = integer(b, "maxSpacing", d.spacing().max(), 0, 200,
                "Largest gap between two seams in blocks.");
        Supplier<Integer> maxFaults = integer(b, "maxFaults", d.maxFaults(), 0, 20, "Most faults.");
        b.pop();
        return () -> new CoalParams.TypeParams(new IntRange(minDepth.get(), maxDepth.get()),
                new IntRange(minSeams.get(), maxSeams.get()), new DoubleRange(minThickness.get(), maxThickness.get()),
                new IntRange(minSpacing.get(), maxSpacing.get()), maxFaults.get());
    }

    /**
     * Reads the current generation parameters from the config.
     *
     * @return parameters with contradicting values repaired
     */
    public static CoalParams params() {
        return PARAMS.get();
    }
}
