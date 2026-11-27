package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.OreConfig;
import java.util.List;

/**
 * Indicator mineral trail settings.
 *
 * @param stepLength distance in blocks between two points of the trail path
 * @param maxLength longest trail in blocks
 * @param inertia share of the previous heading kept at each step, 0 to 1
 * @param flatSlope terrain slope in blocks per block at which the trail follows the downhill direction fully
 * @param startWidth half width of the trail at the deposit in blocks
 * @param widthGrowth growth of the half width per block of trail length
 * @param coldFactor trail length factor for deposits in cold biomes
 * @param dryFactor trail length factor for deposits in dry biomes
 * @param oceanFactor trail length factor for deposits in oceans
 * @param maxSeaDepth depth in blocks below the sea surface at which a trail ends
 * @param indicators spread of each indicator mineral, the lead indicator first
 */
public record PlumeParams(double stepLength, double maxLength, double inertia, double flatSlope,
        double startWidth, double widthGrowth, double coldFactor, double dryFactor, double oceanFactor,
        double maxSeaDepth, List<IndicatorParams> indicators) {
    /**
     * Gives the indicator that rates a trail: the strongest trail, map spots and the grade tone follow it.
     *
     * @return the first indicator
     */
    public IndicatorParams lead() {
        return indicators.get(0);
    }

    /**
     * Gives the default spread of the indicator minerals of a deposit from its data file.
     *
     * @param depositId deposit id
     * @return spread of each indicator, in the order of the data file
     */
    public static List<IndicatorParams> indicatorsOf(String depositId) {
        return OreConfig.registry().deposits().get(depositId).indicators().stream()
                .map(indicator -> new IndicatorParams(indicator.id(), indicator.length(), indicator.peak())).toList();
    }

    /**
     * Spread of one indicator mineral.
     *
     * @param id indicator id
     * @param length trail length in blocks over which the grains fall to 1/e
     * @param peak density of the mineral next to the deposit, 0 to 1; 1 is 100 g per wash
     */
    public record IndicatorParams(String id, double length, double peak) {
    }
}
