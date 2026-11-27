package de.wirezat.realisticores.indicator;

import java.util.List;

/**
 * Downhill path along which the indicator grains of one deposit are spread.
 *
 * @param source the deposit
 * @param xs X of the path points in blocks
 * @param zs Z of the path points in blocks
 * @param lengths distance in blocks from the deposit along the path to each point
 * @param factor length factor of the deposit's climate that stretches or shortens the grain spread
 * @param <S> kind of deposit
 */
public record PlumePath<S extends TrailSource>(S source, double[] xs, double[] zs, double[] lengths, double factor)
        implements Trail {
    @Override
    public List<GuidePoint> guide(double x, double z, double reach, double spacing) {
        return Plume.guide(this, x, z, reach, spacing);
    }
}
