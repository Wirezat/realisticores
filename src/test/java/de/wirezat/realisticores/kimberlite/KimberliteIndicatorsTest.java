package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.indicator.GradeTone;
import de.wirezat.realisticores.indicator.Grains;
import de.wirezat.realisticores.indicator.Measurement;
import de.wirezat.realisticores.indicator.PlumeParams;
import de.wirezat.realisticores.indicator.PlumePath;
import java.util.List;
import org.junit.jupiter.api.Test;

class KimberliteIndicatorsTest {
    private static final PlumeParams PARAMS = KimberliteParams.defaults().plume();
    private static final PipeGrade GRADE = new PipeGrade(KimberliteParams.defaults().grade());
    private static final PlumePath<Pipe> TRAIL = trailFrom(0);

    private static PlumePath<Pipe> trailFrom(int x) {
        return new PlumePath<>(Pipe.of(0L, x, 0, 70), new double[] {x, x + 1600}, new double[] {0, 0},
                new double[] {0, 1600}, 1.0);
    }

    private static Grains grains(int x, int z) {
        return KimberliteIndicators.combine(List.of(TRAIL), PARAMS, GRADE, 42L, x, z).grains();
    }

    private static double ratio(Grains grains) {
        return (double) grains.get("pyrope") / grains.get("chromite");
    }

    @Test
    void valuesFallWithTheWayAndKeepTheirRatioSideways() {
        assertTrue(grains(200, 0).get("pyrope") > grains(400, 0).get("pyrope"));
        assertTrue(grains(400, 0).get("pyrope") > grains(800, 0).get("pyrope"));
        assertTrue(grains(400, 0).get("pyrope") > grains(400, 100).get("pyrope"));
        assertEquals(1.0, ratio(grains(400, 0)) / ratio(grains(400, 100)), 0.23);
    }

    @Test
    void onlyTheStrongestTrailCounts() {
        PlumePath<Pipe> weak = trailFrom(-600);
        Measurement alone = KimberliteIndicators.combine(List.of(TRAIL), PARAMS, GRADE, 42L, 100, 0);
        Measurement both = KimberliteIndicators.combine(List.of(weak, TRAIL), PARAMS, GRADE, 42L, 100, 0);
        assertEquals(alone.grains(), both.grains());
        assertSame(TRAIL, both.source());
    }

    @Test
    void toneTellsTheGradeOfTheStrongestTrailOnceThereIsEnoughPyrope() {
        assertEquals(GradeTone.VIOLET, GradeTone.of(1.0, 50));
        assertEquals(GradeTone.RED, GradeTone.of(0.15, 50));
        assertEquals(GradeTone.BROWN, GradeTone.of(0.0, 50));
        assertEquals(GradeTone.PALE, GradeTone.of(1.0, 49));
        PlumePath<Pipe> weak = trailFrom(-600);
        Measurement both = KimberliteIndicators.combine(List.of(weak, TRAIL), PARAMS, GRADE, 42L, 100, 0);
        assertEquals(GradeTone.of(GRADE.pipeFactor(TRAIL.source()), both.grains().get("pyrope")), both.tone());
    }
}
