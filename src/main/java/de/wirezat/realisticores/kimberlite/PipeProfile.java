package de.wirezat.realisticores.kimberlite;

/** Radius of a pipe by height: crater flare, near-cylindrical diatreme, narrowing root. */
public final class PipeProfile {
    private final KimberliteParams.PipeParams params;

    /**
     * Creates a profile.
     *
     * @param params shape settings of the pipe
     */
    public PipeProfile(KimberliteParams.PipeParams params) {
        this.params = params;
    }

    /**
     * Computes the pipe radius at a height.
     *
     * @param pipe the pipe
     * @param y world Y level
     * @return radius in blocks, never negative
     */
    public double radiusAt(Pipe pipe, int y) {
        double surfaceRadius = pipe.diameter() / 2.0;
        if (y > params.craterBaseY()) {
            double t = clamp((y - params.craterBaseY()) / (double) params.craterHeight());
            return surfaceRadius * lerp(1.0, params.craterWidening(), t);
        }
        if (y > params.rootY()) {
            double t = (params.craterBaseY() - y) / (double) (params.craterBaseY() - params.rootY());
            return surfaceRadius * (1.0 - params.diatremeTaper() * t);
        }
        double rootTop = surfaceRadius * (1.0 - params.diatremeTaper());
        double t = clamp((params.rootY() - y) / (double) (params.rootY() - params.bottomY()));
        return Math.max(lerp(rootTop, params.rootMinRadius(), t), 0.0);
    }

    /**
     * Gives the widest radius the pipe reaches at any height.
     *
     * @param pipe the pipe
     * @return maximum radius in blocks
     */
    public double maxRadius(Pipe pipe) {
        return pipe.diameter() / 2.0 * Math.max(1.0, params.craterWidening());
    }

    private static double clamp(double value) {
        return Math.min(1.0, Math.max(0.0, value));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
