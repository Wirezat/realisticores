package de.wirezat.realisticores.porphyry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.indicator.Grains;
import de.wirezat.realisticores.indicator.PlumeParams;
import de.wirezat.realisticores.indicator.PlumePath;
import java.util.List;
import org.junit.jupiter.api.Test;

class PorphyryIndicatorsTest {
    @Test
    void chalcopyriteFadesFirstAndTourmalineCarriesFarthestDownTheTrail() {
        PlumeParams params = PorphyryParams.defaults().plume();
        Body body = new Body(0, 0, 60, 1.0, 0.0, Stage.MODERATE, 110, 7L, 0.0, 0.0, List.of(), List.of());
        PlumePath<Body> trail = new PlumePath<>(body, new double[] {0, 1500}, new double[] {0, 0},
                new double[] {0, 1500}, 1.0);
        Grains near = PorphyryIndicators.combine(List.of(trail), params, 42L, 100, 0).grains();
        Grains far = PorphyryIndicators.combine(List.of(trail), params, 42L, 700, 0).grains();
        assertTrue(near.get("chalcopyrite") > 0 && near.get("gold") > 0);
        assertEquals(0, far.get("chalcopyrite"));
        assertTrue(far.get("tourmaline") > far.get("gold"));
    }
}
