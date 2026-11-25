package de.wirezat.realisticores.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.kimberlite.Pipe;
import de.wirezat.realisticores.kimberlite.PipeGrade;
import de.wirezat.realisticores.kimberlite.PlumePath;
import java.util.List;
import org.junit.jupiter.api.Test;

class PanYieldTest {
    private static final KimberliteParams.PlumeParams PARAMS = KimberliteParams.defaults().plume();
    private static final PipeGrade GRADE = new PipeGrade(KimberliteParams.defaults().grade());
    private static final PlumePath TRAIL = trailFrom(0);

    private static PlumePath trailFrom(int x) {
        return new PlumePath(new Pipe(x, 0, 70), new double[] {x, x + 1600}, new double[] {0, 0},
                new double[] {0, 1600}, 1.0);
    }

    private static Grains grains(int x, int z) {
        return PanYield.combine(List.of(TRAIL), PARAMS, GRADE, 42L, x, z).grains();
    }

    private static double ratio(Grains grains) {
        return (double) grains.pyrope() / grains.chromite();
    }

    @Test
    void valuesFallWithTheWayAndKeepTheirRatioSideways() {
        assertTrue(grains(200, 0).pyrope() > grains(400, 0).pyrope());
        assertTrue(grains(400, 0).pyrope() > grains(800, 0).pyrope());
        assertTrue(grains(400, 0).pyrope() > grains(400, 100).pyrope());
        assertEquals(1.0, ratio(grains(400, 0)) / ratio(grains(400, 100)), 0.23);
    }

    @Test
    void onlyTheStrongestTrailCounts() {
        PlumePath weak = trailFrom(-600);
        PanYield.Measurement alone = PanYield.combine(List.of(TRAIL), PARAMS, GRADE, 42L, 100, 0);
        PanYield.Measurement both = PanYield.combine(List.of(weak, TRAIL), PARAMS, GRADE, 42L, 100, 0);
        assertEquals(alone.grains(), both.grains());
        assertSame(TRAIL, both.source());
    }

    @Test
    void toneTellsTheGradeOfTheStrongestTrailOnceThereIsEnoughPyrope() {
        assertEquals(PyropeTone.VIOLET, PyropeTone.of(1.0, 50));
        assertEquals(PyropeTone.RED, PyropeTone.of(0.15, 50));
        assertEquals(PyropeTone.BROWN, PyropeTone.of(0.0, 50));
        assertEquals(PyropeTone.PALE, PyropeTone.of(1.0, 49));
        PlumePath weak = trailFrom(-600);
        PanYield.Measurement both = PanYield.combine(List.of(weak, TRAIL), PARAMS, GRADE, 42L, 100, 0);
        assertEquals(PyropeTone.of(GRADE.pipeFactor(TRAIL.pipe()), both.grains().pyrope()), both.tone());
    }
}
