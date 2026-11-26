package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;

/** Ore grade of a pipe: barren, poor or rich, varying by rock phase inside the pipe. */
public final class PipeGrade {
    private static final long PIPE_SALT = 0x4752414445504950L;
    private static final long PHASE_SALT = 0x4752414445504841L;

    private final KimberliteParams.GradeParams params;

    /**
     * Creates a grade model.
     *
     * @param params grade settings
     */
    public PipeGrade(KimberliteParams.GradeParams params) {
        this.params = params;
    }

    /**
     * Gives the grade of a whole pipe.
     *
     * @param pipe the pipe
     * @return 0 for barren pipes, the poor factor for poor pipes, 1 for rich pipes
     */
    public double pipeFactor(Pipe pipe) {
        double roll = SplitMix64.unit(SplitMix64.mix(pipe.seed() ^ PIPE_SALT));
        if (roll < params.barrenShare()) {
            return 0.0;
        }
        return roll < params.barrenShare() + params.poorShare() ? params.poorFactor() : 1.0;
    }

    /**
     * Gives the grade at one position, scaling the pipe grade by the rock phase.
     *
     * @param pipe the pipe
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return grade factor, 0 in barren pipes
     */
    public double factor(Pipe pipe, int x, int y, int z) {
        double pipeFactor = pipeFactor(pipe);
        if (pipeFactor == 0.0) {
            return 0.0;
        }
        long seed = SplitMix64.mix(pipe.seed() ^ PHASE_SALT);
        double phase = 1.0 + params.phaseAmplitude() * ValueNoise.sample3(seed, x, y, z, params.phaseWavelength());
        return pipeFactor * Math.max(0.0, phase);
    }
}
