package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.DoubleRange;
import de.wirezat.realisticores.worldgen.IntRange;

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
 * @param accessories sandstone with its roll front and the seam fires
 */
public record CoalParams(BasinParams basin, ReliefParams relief, TypeParams hardCoal, TypeParams lignite,
        TypeParams folded, FoldParams fold, SeamParams seam, RockParams rock, SurfaceParams surface,
        AccessoryParams accessories) {

    /**
     * Position, size and outline of basins.
     *
     * @param cellSize grid cell size in blocks; each cell holds at most one basin
     * @param chance probability that a cell holds a basin
     * @param jitter maximum offset of the centre from the cell centre, as a fraction of the cell size
     * @param radius smallest and largest mean radius in blocks
     * @param minAspect smallest ratio of short to long axis
     * @param outlineNoise fraction of the radius the outline may deviate by noise
     * @param outlineWavelength size of the outline bumps in blocks
     * @param hardCoalShare share of basins with hard coal, the rest holds lignite
     */
    public record BasinParams(int cellSize, double chance, double jitter, IntRange radius, double minAspect,
            double outlineNoise, double outlineWavelength, double hardCoalShare) {
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
     * @param depth smallest and largest depth of the bowl in blocks
     * @param seams fewest and most seams
     * @param thickness smallest and largest seam thickness in blocks
     * @param spacing smallest and largest gap between two seams in blocks
     * @param maxFaults most faults
     */
    public record TypeParams(IntRange depth, IntRange seams, DoubleRange thickness, IntRange spacing,
            int maxFaults) {
        /**
         * Repairs ranges whose upper bound lies below the lower bound.
         *
         * @return a copy with ordered ranges
         */
        private TypeParams sanitized() {
            return new TypeParams(depth.ordered(), seams.ordered(), thickness.ordered(), spacing.ordered(), maxFaults);
        }
    }

    /**
     * Folds and core of folded basins.
     *
     * @param amplitude smallest and largest fold height in blocks
     * @param wavelength smallest and largest distance between two folds in blocks
     * @param coreShare part of the basin radius that holds anthracite
     */
    public record FoldParams(DoubleRange amplitude, DoubleRange wavelength, double coreShare) {
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
     * @param faultThrow smallest and largest shift at a fault in blocks
     * @param faultWaveAmplitude how far a fault line bends in blocks
     * @param faultWaveWavelength size of the bends in blocks
     * @param outcropThreshold noise value (-1..1) above which a seam shows through the soil
     * @param outcropWavelength size of outcrops in blocks
     */
    public record SeamParams(double cover, double thicknessBase, double thicknessNoise, double thicknessWavelength,
            double pinchOut, double minThickness, double waveAmplitude, double waveWavelength,
            double splitThreshold, double splitWavelength, int maxParting, DoubleRange faultThrow,
            double faultWaveAmplitude, double faultWaveWavelength, double outcropThreshold,
            double outcropWavelength) {
    }

    /**
     * Thickness of the rocks around a seam.
     *
     * @param shale thinnest and thickest shale in blocks
     * @param blackShale thinnest and thickest black shale above a seam in blocks
     * @param fireclay thinnest and thickest fireclay below a seam in blocks
     * @param roofMudstone thinnest and thickest mudstone above lignite in blocks
     * @param floorMudstone thinnest and thickest mudstone below lignite in blocks
     * @param wavelength size of thick and thin zones in blocks
     */
    public record RockParams(IntRange shale, IntRange blackShale, IntRange fireclay, IntRange roofMudstone,
            IntRange floorMudstone, double wavelength) {
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
     * Sandstone with its roll front and the seam fires.
     *
     * @param sand thinnest and thickest sandstone above a hard coal seam in blocks
     * @param ligniteSand thinnest and thickest sandstone above a lignite seam in blocks
     * @param sandCover share of the area with sandstone
     * @param sandWavelength size of sandstone lenses in blocks
     * @param front innermost and outermost position of the roll front, as distance to the centre in basin units
     * @param frontBand width in blocks of the ore band inside the front
     * @param fireCell grid cell size of seam fires in blocks
     * @param fireChance probability that a cell holds a fire
     * @param fireRadius smallest and largest fire radius in blocks
     * @param fireDepth blocks below the surface that burn
     * @param blackbandCover share of the area where blackband ironstone lies on the topmost hard coal seam
     * @param rustDepth blocks of shale below the soil that are rust-stained
     */
    public record AccessoryParams(IntRange sand, IntRange ligniteSand, double sandCover, double sandWavelength,
            DoubleRange front, int frontBand, int fireCell, double fireChance, IntRange fireRadius, int fireDepth,
            double blackbandCover, int rustDepth) {
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
        int minRadius = Math.min(b.radius().min(), largest);
        IntRange radius = new IntRange(minRadius, Math.max(minRadius, Math.min(b.radius().max(), largest)));
        SeamParams s = seam;
        RockParams r = rock;
        AccessoryParams a = accessories;
        return new CoalParams(
                new BasinParams(b.cellSize(), b.chance(), b.jitter(), radius, b.minAspect(), b.outlineNoise(),
                        b.outlineWavelength(), b.hardCoalShare()),
                relief, hardCoal.sanitized(), lignite.sanitized(), folded.sanitized(),
                new FoldParams(fold.amplitude().ordered(), fold.wavelength().ordered(), fold.coreShare()),
                new SeamParams(s.cover(), s.thicknessBase(), s.thicknessNoise(), s.thicknessWavelength(),
                        s.pinchOut(), s.minThickness(), s.waveAmplitude(), s.waveWavelength(), s.splitThreshold(),
                        s.splitWavelength(), s.maxParting(), s.faultThrow().ordered(), s.faultWaveAmplitude(),
                        s.faultWaveWavelength(), s.outcropThreshold(), s.outcropWavelength()),
                new RockParams(r.shale().ordered(), r.blackShale().ordered(), r.fireclay().ordered(),
                        r.roofMudstone().ordered(), r.floorMudstone().ordered(), r.wavelength()),
                surface, new AccessoryParams(a.sand().ordered(), a.ligniteSand().ordered(), a.sandCover(),
                        a.sandWavelength(), a.front().ordered(), a.frontBand(), a.fireCell(), a.fireChance(),
                        a.fireRadius().ordered(), a.fireDepth(), a.blackbandCover(), a.rustDepth()));
    }

    /**
     * Returns the default values from docs/superpowers/specs/2026-11-25-coal-basin-design.md.
     *
     * @return default parameter set
     */
    public static CoalParams defaults() {
        return new CoalParams(
                new BasinParams(2000, 0.9, 0.13, new IntRange(400, 730), 0.6, 0.15, 250.0, 0.65),
                new ReliefParams(100, 50, 2),
                typeParams(40, 90, 1, 4, 1.0, 3.0, 5, 20, 4),
                typeParams(15, 35, 1, 2, 3.0, 8.0, 5, 10, 1),
                typeParams(60, 110, 2, 4, 1.0, 3.0, 8, 20, 4),
                new FoldParams(new DoubleRange(10.0, 18.0), new DoubleRange(200.0, 300.0), 0.55),
                new SeamParams(6.0, 1.25, 0.6, 180.0, 0.1, 0.5, 6.0, 300.0, 0.4, 400.0, 3,
                        new DoubleRange(4.0, 10.0), 10.0, 200.0, -0.125, 14.0),
                new RockParams(new IntRange(1, 2), new IntRange(1, 2), new IntRange(1, 2), new IntRange(3, 5),
                        new IntRange(2, 4), 60.0),
                new SurfaceParams(0.04, 8, -0.125, 14.0, 2),
                new AccessoryParams(new IntRange(2, 4), new IntRange(3, 7), 0.5, 120.0, new DoubleRange(0.7, 0.85),
                        4, 40, 0.4, new IntRange(3, 6), 6, 0.3, 2));
    }

    /**
     * Creates the settings of a basin type from plain bounds.
     *
     * @param minDepth smallest bowl depth
     * @param maxDepth largest bowl depth
     * @param minSeams fewest seams
     * @param maxSeams most seams
     * @param minThickness thinnest seam
     * @param maxThickness thickest seam
     * @param minSpacing smallest gap between seams
     * @param maxSpacing largest gap between seams
     * @param maxFaults most faults
     * @return the settings
     */
    private static TypeParams typeParams(int minDepth, int maxDepth, int minSeams, int maxSeams, double minThickness,
            double maxThickness, int minSpacing, int maxSpacing, int maxFaults) {
        return new TypeParams(new IntRange(minDepth, maxDepth), new IntRange(minSeams, maxSeams),
                new DoubleRange(minThickness, maxThickness), new IntRange(minSpacing, maxSpacing), maxFaults);
    }
}
