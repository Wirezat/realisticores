package de.wirezat.realisticores.kimberlite;

/**
 * All tunable values of kimberlite generation.
 *
 * @param craton craton mask settings
 * @param field kimberlite field settings
 * @param pipe pipe shape and size settings
 */
public record KimberliteParams(CratonParams craton, FieldParams field, PipeParams pipe) {

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
     */
    public record PipeParams(int medianDiameter, double diameterSigma, int minDiameter, int maxDiameter,
            int craterBaseY, int craterHeight, double craterWidening, double diatremeTaper,
            int rootY, double rootMinRadius, int bottomY) {
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
                new PipeParams(70, 0.5, 30, 300, 64, 16, 1.2, 0.15, -40, 2.5, -64));
    }
}
