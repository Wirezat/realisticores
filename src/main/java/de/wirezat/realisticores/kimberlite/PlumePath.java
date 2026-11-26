package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.indicator.GuidePoint;
import de.wirezat.realisticores.indicator.Trail;
import java.util.List;

/**
 * Downhill path along which the indicator grains of one pipe are spread.
 *
 * @param pipe the source pipe
 * @param xs X of the path points in blocks
 * @param zs Z of the path points in blocks
 * @param lengths distance in blocks from the pipe along the path to each point
 * @param factor length factor of the pipe's climate that stretches or shortens the grain spread
 */
public record PlumePath(Pipe pipe, double[] xs, double[] zs, double[] lengths, double factor) implements Trail {
    @Override
    public List<GuidePoint> guide(double x, double z, double reach, double spacing) {
        return Plume.guide(this, x, z, reach, spacing);
    }
}
