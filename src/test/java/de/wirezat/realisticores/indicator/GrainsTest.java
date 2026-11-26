package de.wirezat.realisticores.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GrainsTest {
    private static Grains grains(int pyrope, int chromite, int ilmenite) {
        return new Grains(Map.of("pyrope", pyrope, "chromite", chromite, "ilmenite", ilmenite));
    }

    @Test
    void noiseIsFixedPerColumnAndWithinFivePercent() {
        Map<String, Double> densities = new LinkedHashMap<>();
        densities.put("pyrope", 0.6);
        densities.put("chromite", 0.4);
        densities.put("ilmenite", 0.5);
        Grains first = Grains.measure(densities, 42L, 100, 200);
        assertEquals(first, Grains.measure(densities, 42L, 100, 200));
        assertNotEquals(first, Grains.measure(densities, 42L, 101, 200));
        assertTrue(first.get("pyrope") >= 570 && first.get("pyrope") <= 630);
    }

    @Test
    void everyFullHundredBecomesAnItemAndTheRestStays() {
        Grains sum = grains(950, 0, 10).plus(grains(1150, 40, 0));
        assertEquals(grains(2, 0, 0), sum.whole());
        assertEquals(grains(100, 40, 10), sum.rest());
    }

    @Test
    void fortuneAndSilkTouchScaleOnlyTheGain() {
        Grains measured = grains(200, 100, 0);
        assertEquals(grains(300, 150, 0), measured.gain(1, false));
        assertEquals(grains(500, 250, 0), measured.gain(3, false));
        assertEquals(grains(100, 50, 0), measured.gain(0, true));
    }
}
