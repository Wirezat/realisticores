package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PipeGradeTest {
    private static final KimberliteParams.GradeParams PARAMS = KimberliteParams.defaults().grade();
    private static final PipeGrade GRADE = new PipeGrade(PARAMS);

    @Test
    void pipesSplitIntoBarrenPoorAndRichByTheConfiguredShares() {
        int barren = 0;
        int poor = 0;
        int rich = 0;
        int total = 4000;
        for (int i = 0; i < total; i++) {
            double factor = GRADE.pipeFactor(new Pipe(i * 211, i * 97 - 5000, 80));
            if (factor == 0.0) {
                barren++;
            } else if (factor == PARAMS.poorFactor()) {
                poor++;
            } else {
                rich++;
            }
        }
        assertEquals(PARAMS.barrenShare(), barren / (double) total, 0.04);
        assertEquals(PARAMS.poorShare(), poor / (double) total, 0.04);
        assertEquals(1.0 - PARAMS.barrenShare() - PARAMS.poorShare(), rich / (double) total, 0.04);
    }

    @Test
    void barrenPipesHaveNoOreAnywhereAndRichPipesVaryByPhase() {
        Pipe barren = null;
        Pipe rich = null;
        for (int i = 0; i < 200 && (barren == null || rich == null); i++) {
            Pipe pipe = new Pipe(i * 131, i * 53, 80);
            if (GRADE.pipeFactor(pipe) == 0.0 && barren == null) {
                barren = pipe;
            } else if (GRADE.pipeFactor(pipe) == 1.0 && rich == null) {
                rich = pipe;
            }
        }
        for (int x = 0; x < 100; x += 7) {
            assertEquals(0.0, GRADE.factor(barren, x, 0, x));
        }
        double min = Double.MAX_VALUE;
        double max = 0.0;
        for (int x = 0; x < 400; x += 9) {
            double factor = GRADE.factor(rich, x, -20, 2 * x);
            min = Math.min(min, factor);
            max = Math.max(max, factor);
        }
        assertTrue(max - min > 0.5, "min=" + min + " max=" + max);
    }
}
