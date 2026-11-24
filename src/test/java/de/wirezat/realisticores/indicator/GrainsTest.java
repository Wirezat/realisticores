package de.wirezat.realisticores.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrainsTest {
    @Test
    void noiseIsFixedPerColumnAndWithinFivePercent() {
        Grains first = Grains.measure(0.6, 0.4, 0.5, 42L, 100, 200);
        assertEquals(first, Grains.measure(0.6, 0.4, 0.5, 42L, 100, 200));
        assertNotEquals(first, Grains.measure(0.6, 0.4, 0.5, 42L, 101, 200));
        assertTrue(first.pyrope() >= 570 && first.pyrope() <= 630);
    }

    @Test
    void everyFullHundredBecomesAnItemAndTheRestStays() {
        Grains sum = new Grains(950, 0, 10).plus(new Grains(1150, 40, 0));
        assertEquals(new Grains(2, 0, 0), sum.whole());
        assertEquals(new Grains(100, 40, 10), sum.rest());
    }

    @Test
    void fortuneAndSilkTouchScaleOnlyTheGain() {
        Grains measured = new Grains(200, 100, 0);
        assertEquals(new Grains(300, 150, 0), measured.gain(1, false));
        assertEquals(new Grains(500, 250, 0), measured.gain(3, false));
        assertEquals(new Grains(100, 50, 0), measured.gain(0, true));
    }
}
