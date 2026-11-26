package de.wirezat.realisticores.indicator;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PanYieldTest {
    @Test
    void theDepositWithTheMostGrainsWins() {
        Measurement weak = new Measurement(new Grains(Map.of("pyrope", 40)), null, GradeTone.PALE);
        Measurement strong = new Measurement(new Grains(Map.of("zircon", 30, "rutile", 20)), null, GradeTone.PALE);
        assertSame(strong, PanYield.strongest(List.of(weak, strong)));
        assertSame(Measurement.NONE, PanYield.strongest(List.of()));
    }
}
