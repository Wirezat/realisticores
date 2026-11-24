package de.wirezat.realisticores.kimberlite;

/**
 * All tunable values of kimberlite generation.
 *
 * @param craton craton mask settings
 * @param field kimberlite field settings
 * @param pipe pipe shape and size settings
 * @param facies rock type settings inside pipes
 * @param crater crater bowl settings
 * @param grade ore grade settings of pipes
 */
public record KimberliteParams(CratonParams craton, FieldParams field, PipeParams pipe, FaciesParams facies,
        CraterParams crater, GradeParams grade) {

    /**
     * Craton mask settings.
     *
     * @param wavelength noise wavelength in blocks
     * @param threshold noise value (-1..1) above which a position counts as craton
     */
    public record CratonParams(double wavelength, double threshold) {
    }

    /**
     * Kimberlite field settings.
     *
     * @param cellSize grid cell size in blocks; each cell holds at most one field
     * @param chance probability that a cell holds a field
     * @param jitter maximum offset of the field centre from the cell centre, as a fraction of the cell size
     * @param minRadius smallest field radius in blocks
     * @param maxRadius largest field radius in blocks
     * @param minPipes fewest pipes attempted per field
     * @param maxPipes most pipes attempted per field
     * @param minSpacing minimum distance between pipe centres in blocks
     * @param placementAttempts tries to find a free position for each pipe before it is skipped
     */
    public record FieldParams(int cellSize, double chance, double jitter, int minRadius, int maxRadius,
            int minPipes, int maxPipes, int minSpacing, int placementAttempts) {
    }

    /**
     * Pipe shape and size settings.
     *
     * @param medianDiameter median surface diameter in blocks
     * @param diameterSigma standard deviation of the natural log of the diameter
     * @param minDiameter smallest allowed diameter in blocks
     * @param maxDiameter largest allowed diameter in blocks
     * @param craterBaseY Y level where the crater flare starts
     * @param craterHeight height of the crater flare in blocks
     * @param craterWidening radius factor reached at the top of the crater flare
     * @param diatremeTaper fraction of the radius lost between craterBaseY and rootY
     * @param rootY Y level where the diatreme turns into the root
     * @param rootMinRadius radius in blocks reached at bottomY
     * @param bottomY lowest Y level of the pipe
     * @param wallNoiseStrength fraction of the radius the wall may deviate by noise
     * @param minAspect smallest ratio of short to long axis of the outline
     * @param outcropThreshold noise value (-1..1) above which a surface column exposes kimberlite
     * @param outcropWavelength wavelength of the outcrop noise in blocks
     * @param wallWavelengthFactor size of the large wall bumps as a fraction of the diameter
     * @param wallMinWavelength smallest size of the large wall bumps in blocks
     * @param wallFineShare share of the fine wall roughness in the wall noise, between 0 and 1
     * @param wallFineScale size of the fine wall roughness relative to the large bumps
     * @param wallVerticalStretch how much the wall noise is stretched vertically
     */
    public record PipeParams(int medianDiameter, double diameterSigma, int minDiameter, int maxDiameter,
            int craterBaseY, int craterHeight, double craterWidening, double diatremeTaper,
            int rootY, double rootMinRadius, int bottomY, double wallNoiseStrength, double minAspect,
            double outcropThreshold, double outcropWavelength, double wallWavelengthFactor,
            double wallMinWavelength, double wallFineShare, double wallFineScale, double wallVerticalStretch) {
    }

    /**
     * Rock type settings inside pipes.
     *
     * @param weatheringDepth depth below the surface in blocks down to which kimberlite is weathered
     * @param weatheringNoise variation of the weathering depth in blocks
     * @param contactFraction outer fraction of the radius that forms the contact breccia
     * @param serpentinizationBand depth in blocks below the weathering zone with more serpentinization
     * @param serpentinizationThreshold noise value (-1..1) above which kimberlite is serpentinized at depth
     * @param xenolithThreshold noise value (-1..1) above which a crustal xenolith replaces the rock
     * @param peridotiteThreshold noise value (-1..1) above which a mantle xenolith replaces the rock
     * @param contactWavelength size of the host-rock fragments in the contact breccia in blocks
     * @param xenolithWavelength size of crustal xenoliths in blocks
     * @param peridotiteWavelength size of mantle xenoliths in blocks
     * @param serpentineWavelength size of serpentinized patches in blocks
     * @param weatheringWavelength size of the bumps of the weathering depth in blocks
     * @param serpentineBandBonus extra serpentinization in the band below the weathering zone
     * @param serpentineWallBonus extra serpentinization near the pipe wall
     * @param serpentineWallStart wall distance (0 axis, 1 wall) from which the wall bonus applies
     * @param contactThreshold noise value (-1..1) above which a contact zone block stays host rock
     */
    public record FaciesParams(double weatheringDepth, double weatheringNoise, double contactFraction,
            double serpentinizationBand, double serpentinizationThreshold, double xenolithThreshold,
            double peridotiteThreshold, double contactWavelength, double xenolithWavelength,
            double peridotiteWavelength, double serpentineWavelength, double weatheringWavelength,
            double serpentineBandBonus, double serpentineWallBonus, double serpentineWallStart,
            double contactThreshold) {
    }

    /**
     * Crater bowl settings.
     *
     * @param depthPerDiameter bowl depth as a fraction of the pipe diameter
     * @param minDepth smallest bowl depth in blocks
     * @param maxDepth largest bowl depth in blocks
     * @param floorLayers number of volcaniclastic layers at the bowl floor
     * @param ringPoints number of terrain samples on the ring around a crater
     */
    public record CraterParams(double depthPerDiameter, int minDepth, int maxDepth, int floorLayers,
            int ringPoints) {
    }

    /**
     * Ore grade settings of pipes.
     *
     * @param barrenShare share of pipes without any graded ore
     * @param poorShare share of pipes with a reduced ore share
     * @param poorFactor factor applied to the ore share of poor pipes
     * @param phaseWavelength size of the rock phases in blocks, each with its own grade
     * @param phaseAmplitude how far the grade of a phase may deviate from the pipe grade, as a fraction
     */
    public record GradeParams(double barrenShare, double poorShare, double poorFactor, double phaseWavelength,
            double phaseAmplitude) {
    }

    /**
     * Repairs values that contradict each other: upper bounds below their lower bounds, a root above the crater
     * base, a bottom above the root, and grade shares that exceed 100 %.
     *
     * @return a copy where every range is ordered and the grade shares fit together
     */
    public KimberliteParams sanitized() {
        FieldParams f = field;
        FieldParams field = new FieldParams(f.cellSize(), f.chance(), f.jitter(), f.minRadius(),
                Math.max(f.minRadius(), f.maxRadius()), f.minPipes(), Math.max(f.minPipes(), f.maxPipes()),
                f.minSpacing(), f.placementAttempts());
        PipeParams p = pipe;
        int rootY = Math.min(p.rootY(), p.craterBaseY() - 1);
        PipeParams pipe = new PipeParams(p.medianDiameter(), p.diameterSigma(), p.minDiameter(),
                Math.max(p.minDiameter(), p.maxDiameter()), p.craterBaseY(), p.craterHeight(),
                p.craterWidening(), p.diatremeTaper(), rootY, p.rootMinRadius(),
                Math.min(p.bottomY(), rootY - 1), p.wallNoiseStrength(), p.minAspect(), p.outcropThreshold(),
                p.outcropWavelength(), p.wallWavelengthFactor(), p.wallMinWavelength(), p.wallFineShare(),
                p.wallFineScale(), p.wallVerticalStretch());
        CraterParams c = crater;
        CraterParams crater = new CraterParams(c.depthPerDiameter(), c.minDepth(),
                Math.max(c.minDepth(), c.maxDepth()), c.floorLayers(), c.ringPoints());
        GradeParams g = grade;
        double poorShare = Math.min(g.poorShare(), 1.0 - Math.min(1.0, g.barrenShare()));
        GradeParams grade = new GradeParams(g.barrenShare(), poorShare, g.poorFactor(), g.phaseWavelength(),
                g.phaseAmplitude());
        return new KimberliteParams(craton, field, pipe, facies, crater, grade);
    }

    /**
     * Returns the default start values from docs/ores/diamond-phase1-kimberlite.md.
     *
     * @return default parameter set
     */
    public static KimberliteParams defaults() {
        return new KimberliteParams(
                new CratonParams(8000.0, 0.45),
                new FieldParams(3000, 0.5, 0.3, 300, 800, 5, 30, 80, 10),
                new PipeParams(70, 0.5, 30, 300, 64, 16, 1.2, 0.15, -40, 2.5, -64, 0.45, 0.55, 0.5, 14.0, 0.3, 10.0, 0.35, 0.4, 2.0),
                new FaciesParams(25.0, 6.0, 0.15, 35.0, 0.6, 0.56, 0.855, 6.0, 5.0, 3.0, 10.0, 20.0, 0.4, 0.3, 0.8, 0.0),
                new CraterParams(0.08, 4, 12, 3, 12),
                new GradeParams(0.5, 0.35, 0.15, 40.0, 0.9));
    }
}
