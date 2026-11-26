package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FaciesSelectorTest {
    private static final KimberliteParams PARAMS = KimberliteParams.defaults();
    private static final FaciesSelector SELECTOR = new FaciesSelector(PARAMS);
    private static final Pipe PIPE = Pipe.of(0L, 500, 500, 120);

    private static Map<Facies, Integer> histogram(int y, int surfaceY, double wallDistance) {
        Map<Facies, Integer> counts = new EnumMap<>(Facies.class);
        for (int x = 0; x < 120; x++) {
            for (int z = 0; z < 120; z++) {
                counts.merge(SELECTOR.at(PIPE, 440 + x, y, 440 + z, surfaceY, wallDistance), 1, Integer::sum);
            }
        }
        return counts;
    }

    private static Facies dominant(Map<Facies, Integer> counts) {
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }

    @Test
    void topOfThePipeIsWeathered() {
        assertEquals(Facies.WEATHERED, dominant(histogram(60, 66, 0.3)));
    }

    @Test
    void dominantRockFollowsDepthZone() {
        assertEquals(Facies.BRECCIA, dominant(histogram(0, 70, 0.3)));
        assertEquals(Facies.KIMBERLITE, dominant(histogram(-55, 70, 0.3)));
        assertEquals(Facies.VOLCANICLASTIC, dominant(histogram(100, 200, 0.3)));
    }

    @Test
    void contactZoneMixesHostRockAndBreccia() {
        Map<Facies, Integer> counts = histogram(0, 70, 0.95);
        assertTrue(counts.getOrDefault(Facies.HOST, 0) > 1000);
        assertTrue(counts.getOrDefault(Facies.BRECCIA, 0) > 1000);
    }

    @Test
    void serpentinizationIsStrongerBelowTheWeatheringZoneThanDeeper() {
        int band = histogram(30, 70, 0.3).getOrDefault(Facies.SERPENTINIZED, 0);
        int deep = histogram(-30, 70, 0.3).getOrDefault(Facies.SERPENTINIZED, 0);
        assertTrue(band > deep * 2, "band=" + band + " deep=" + deep);
    }
}
