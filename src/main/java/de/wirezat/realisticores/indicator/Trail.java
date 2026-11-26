package de.wirezat.realisticores.indicator;

import java.util.List;

/** The way indicator grains took from their deposit, which the trail sense enchantment shows. */
public interface Trail {
    /**
     * Lists evenly spaced points from a position along the trail back toward its deposit.
     *
     * @param x world X in blocks
     * @param z world Z in blocks
     * @param reach distance in blocks to cover, counted from the position
     * @param spacing distance in blocks between two points
     * @return points ordered from the position toward the deposit
     */
    List<GuidePoint> guide(double x, double z, double reach, double spacing);
}
