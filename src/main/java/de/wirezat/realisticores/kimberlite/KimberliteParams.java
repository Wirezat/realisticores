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
     */
    public record FieldParams(int cellSize, double chance, double jitter, int minRadius, int maxRadius,
            int minPipes, int maxPipes, int minSpacing) {
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
     */
    public record PipeParams(int medianDiameter, double diameterSigma, int minDiameter, int maxDiameter,
            int craterBaseY, int craterHeight, double craterWidening, double diatremeTaper,
            int rootY, double rootMinRadius, int bottomY, double wallNoiseStrength, double minAspect,
            double outcropThreshold, double outcropWavelength) {
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
     */
    public record FaciesParams(double weatheringDepth, double weatheringNoise, double contactFraction,
            double serpentinizationBand, double serpentinizationThreshold, double xenolithThreshold,
            double peridotiteThreshold) {
    }

    /**
     * Crater bowl settings.
     *
     * @param depthPerDiameter bowl depth as a fraction of the pipe diameter
     * @param minDepth smallest bowl depth in blocks
     * @param maxDepth largest bowl depth in blocks
     * @param floorLayers number of volcaniclastic layers at the bowl floor
     */
    public record CraterParams(double depthPerDiameter, int minDepth, int maxDepth, int floorLayers) {
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
     * Returns the default start values from docs/ores/diamond-phase1-kimberlite.md.
     *
     * @return default parameter set
     */
    public static KimberliteParams defaults() {
        return new KimberliteParams(
                new CratonParams(8000.0, 0.45),
                new FieldParams(3000, 0.5, 0.3, 300, 800, 5, 30, 80),
                new PipeParams(70, 0.5, 30, 300, 64, 16, 1.2, 0.15, -40, 2.5, -64, 0.45, 0.55, 0.5, 14.0),
                new FaciesParams(25.0, 6.0, 0.15, 35.0, 0.6, 0.56, 0.855),
                new CraterParams(0.08, 4, 12, 3),
                new GradeParams(0.5, 0.35, 0.15, 40.0, 0.9));
    }
}
