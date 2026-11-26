package de.wirezat.realisticores.coal;

/**
 * All tunable values of coal basin generation.
 *
 * @param basin position, size and outline of basins
 * @param relief terrain limits
 * @param hardCoal settings of hard coal basins
 * @param lignite settings of lignite basins
 * @param folded settings of folded basins
 * @param fold folds and anthracite core of folded basins
 * @param seam shape of the seams
 * @param rock thickness of the rocks around a seam
 * @param surface loose coal and coaly soil above a seam
 */
public record CoalParams(BasinParams basin, ReliefParams relief, TypeParams hardCoal, TypeParams lignite,
        TypeParams folded, FoldParams fold, SeamParams seam, RockParams rock, SurfaceParams surface) {

    /**
     * Position, size and outline of basins.
     *
     * @param cellSize grid cell size in blocks; each cell holds at most one basin
     * @param chance probability that a cell holds a basin
     * @param jitter maximum offset of the centre from the cell centre, as a fraction of the cell size
     * @param minRadius smallest mean radius in blocks
     * @param maxRadius largest mean radius in blocks
     * @param minAspect smallest ratio of short to long axis
     * @param outlineNoise fraction of the radius the outline may deviate by noise
     * @param outlineWavelength size of the outline bumps in blocks
     * @param hardCoalShare share of basins with hard coal, the rest holds lignite
     */
    public record BasinParams(int cellSize, double chance, double jitter, int minRadius, int maxRadius,
            double minAspect, double outlineNoise, double outlineWavelength, double hardCoalShare) {
    }

    /**
     * Terrain limits.
     *
     * @param mountainRelief height difference in blocks inside a basin above which there is no basin
     * @param foothillRise height in blocks above the basin from which a rim point counts as mountain
     * @param foothillPoints number of mountain points on the rim that make a basin folded
     */
    public record ReliefParams(int mountainRelief, int foothillRise, int foothillPoints) {
    }

    /**
     * Settings of one basin type.
     *
     * @param minDepth smallest depth of the bowl in blocks
     * @param maxDepth largest depth of the bowl in blocks
     * @param minSeams fewest seams
     * @param maxSeams most seams
     * @param minThickness smallest seam thickness in blocks
     * @param maxThickness largest seam thickness in blocks
     * @param minSpacing smallest gap between two seams in blocks
     * @param maxSpacing largest gap between two seams in blocks
     * @param maxFaults most faults
     */
    public record TypeParams(int minDepth, int maxDepth, int minSeams, int maxSeams, double minThickness,
            double maxThickness, int minSpacing, int maxSpacing, int maxFaults) {
        private TypeParams sanitized() {
            return new TypeParams(minDepth, Math.max(minDepth, maxDepth), minSeams, Math.max(minSeams, maxSeams),
                    minThickness, Math.max(minThickness, maxThickness), minSpacing,
                    Math.max(minSpacing, maxSpacing), maxFaults);
        }
    }

    /**
     * Folds and core of folded basins.
     *
     * @param minAmplitude smallest fold height in blocks
     * @param maxAmplitude largest fold height in blocks
     * @param minWavelength smallest distance between two folds in blocks
     * @param maxWavelength largest distance between two folds in blocks
     * @param coreShare part of the basin radius that holds anthracite
     */
    public record FoldParams(double minAmplitude, double maxAmplitude, double minWavelength, double maxWavelength,
            double coreShare) {
    }

    /**
     * Shape of the seams.
     *
     * @param cover blocks between the topmost seam and the basin datum at the centre
     * @param thicknessBase constant part of the thickness factor
     * @param thicknessNoise noise part of the thickness factor
     * @param thicknessWavelength size of thick and thin zones in blocks
     * @param pinchOut outer fraction of the basin in which seams thin out
     * @param minThickness thickness in blocks below which there is no seam
     * @param waveAmplitude height of the gentle waves in blocks
     * @param waveWavelength size of the gentle waves in blocks
     * @param splitThreshold noise value (-1..1) above which a seam splits
     * @param splitWavelength size of split zones in blocks
     * @param maxParting thickest parting between two benches in blocks
     * @param minThrow smallest shift at a fault in blocks
     * @param maxThrow largest shift at a fault in blocks
     * @param faultWaveAmplitude how far a fault line bends in blocks
     * @param faultWaveWavelength size of the bends in blocks
     * @param outcropThreshold noise value (-1..1) above which a seam shows through the soil
     * @param outcropWavelength size of outcrops in blocks
     */
    public record SeamParams(double cover, double thicknessBase, double thicknessNoise, double thicknessWavelength,
            double pinchOut, double minThickness, double waveAmplitude, double waveWavelength,
            double splitThreshold, double splitWavelength, int maxParting, double minThrow, double maxThrow,
            double faultWaveAmplitude, double faultWaveWavelength, double outcropThreshold,
            double outcropWavelength) {
    }

    /**
     * Thickness of the rocks around a seam.
     *
     * @param minShale thinnest shale in blocks
     * @param maxShale thickest shale in blocks
     * @param minBlackShale thinnest black shale above a seam in blocks
     * @param maxBlackShale thickest black shale above a seam in blocks
     * @param minFireclay thinnest fireclay below a seam in blocks
     * @param maxFireclay thickest fireclay below a seam in blocks
     * @param minRoofMudstone thinnest mudstone above lignite in blocks
     * @param maxRoofMudstone thickest mudstone above lignite in blocks
     * @param minFloorMudstone thinnest mudstone below lignite in blocks
     * @param maxFloorMudstone thickest mudstone below lignite in blocks
     * @param wavelength size of thick and thin zones in blocks
     */
    public record RockParams(int minShale, int maxShale, int minBlackShale, int maxBlackShale, int minFireclay,
            int maxFireclay, int minRoofMudstone, int maxRoofMudstone, int minFloorMudstone, int maxFloorMudstone,
            double wavelength) {
    }

    /**
     * Loose coal and coaly soil above a seam.
     *
     * @param looseDensity share of outcrop columns with loose coal
     * @param looseMaxHeight height in blocks of an eroded seam above the ground up to which its coal lies around
     * @param soilThreshold noise value (-1..1) above which the soil of an outcrop is coaly
     * @param soilWavelength size of coaly patches in blocks
     * @param soilFringe height in blocks of an eroded seam above the ground up to which coaly soil frays out
     */
    public record SurfaceParams(double looseDensity, int looseMaxHeight, double soilThreshold, double soilWavelength,
            int soilFringe) {
    }

    /**
     * Gives the settings of a basin type.
     *
     * @param type the type
     * @return settings of that type
     */
    public TypeParams type(BasinType type) {
        return switch (type) {
            case HARD_COAL -> hardCoal;
            case LIGNITE -> lignite;
            case FOLDED -> folded;
        };
    }

    /**
     * Repairs upper bounds that lie below their lower bounds and radii that reach beyond the neighbour cells.
     *
     * @return a copy where every range is ordered and every basin stays within its neighbour cells
     */
    public CoalParams sanitized() {
        BasinParams b = basin;
        int largest = (int) (b.cellSize() * (1.5 - b.jitter()) * (1.0 - b.outlineNoise()) * Math.sqrt(b.minAspect()));
        int minRadius = Math.min(b.minRadius(), largest);
        FoldParams f = fold;
        SeamParams s = seam;
        RockParams r = rock;
        return new CoalParams(
                new BasinParams(b.cellSize(), b.chance(), b.jitter(), minRadius,
                        Math.max(minRadius, Math.min(b.maxRadius(), largest)), b.minAspect(), b.outlineNoise(),
                        b.outlineWavelength(), b.hardCoalShare()),
                relief, hardCoal.sanitized(), lignite.sanitized(), folded.sanitized(),
                new FoldParams(f.minAmplitude(), Math.max(f.minAmplitude(), f.maxAmplitude()), f.minWavelength(),
                        Math.max(f.minWavelength(), f.maxWavelength()), f.coreShare()),
                new SeamParams(s.cover(), s.thicknessBase(), s.thicknessNoise(), s.thicknessWavelength(),
                        s.pinchOut(), s.minThickness(), s.waveAmplitude(), s.waveWavelength(), s.splitThreshold(),
                        s.splitWavelength(), s.maxParting(), s.minThrow(), Math.max(s.minThrow(), s.maxThrow()),
                        s.faultWaveAmplitude(), s.faultWaveWavelength(), s.outcropThreshold(),
                        s.outcropWavelength()),
                new RockParams(r.minShale(), Math.max(r.minShale(), r.maxShale()), r.minBlackShale(),
                        Math.max(r.minBlackShale(), r.maxBlackShale()), r.minFireclay(),
                        Math.max(r.minFireclay(), r.maxFireclay()), r.minRoofMudstone(),
                        Math.max(r.minRoofMudstone(), r.maxRoofMudstone()), r.minFloorMudstone(),
                        Math.max(r.minFloorMudstone(), r.maxFloorMudstone()), r.wavelength()),
                surface);
    }

    /**
     * Returns the default values from docs/superpowers/specs/2026-11-25-coal-basin-design.md.
     *
     * @return default parameter set
     */
    public static CoalParams defaults() {
        return new CoalParams(
                new BasinParams(2000, 0.9, 0.13, 400, 730, 0.6, 0.15, 250.0, 0.65),
                new ReliefParams(100, 50, 2),
                new TypeParams(40, 90, 1, 4, 1.0, 3.0, 5, 20, 4),
                new TypeParams(15, 35, 1, 2, 3.0, 8.0, 5, 10, 1),
                new TypeParams(60, 110, 2, 4, 1.0, 3.0, 8, 20, 4),
                new FoldParams(10.0, 18.0, 200.0, 300.0, 0.55),
                new SeamParams(6.0, 1.25, 0.6, 180.0, 0.1, 0.5, 6.0, 300.0, 0.4, 400.0, 3, 4.0, 10.0, 10.0, 200.0,
                        -0.125, 14.0),
                new RockParams(1, 2, 1, 2, 1, 2, 3, 5, 2, 4, 60.0),
                new SurfaceParams(0.04, 8, -0.125, 14.0, 2));
    }
}
