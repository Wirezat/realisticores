package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.worldgen.IntRange;
import org.junit.jupiter.api.Test;

class KimberliteParamsTest {
    @Test
    void sanitizedParamsKeepRangesOrderedAndSharesWithinOneHundredPercent() {
        KimberliteParams d = KimberliteParams.defaults();
        KimberliteParams.PipeParams p = d.pipe();
        KimberliteParams broken = new KimberliteParams(d.craton(),
                new KimberliteParams.FieldParams(3000, 0.5, 0.3, new IntRange(800, 300), new IntRange(30, 5), 80, 10),
                new KimberliteParams.PipeParams(70, 0.5, new IntRange(300, 30), 64, 16, 1.2, 0.15, 90, 2.5, 100,
                        p.wallNoiseStrength(), p.minAspect(), p.outcropThreshold(), p.outcropWavelength(),
                        p.wallWavelengthFactor(), p.wallMinWavelength(), p.wallFineShare(), p.wallFineScale(),
                        p.wallVerticalStretch()),
                d.facies(),
                new KimberliteParams.CraterParams(0.08, new IntRange(12, 4), 3, 12, 8, 3, false),
                new KimberliteParams.GradeParams(0.8, 0.5, 0.15, 40.0, 0.9), d.plume());
        KimberliteParams fixed = broken.sanitized();
        assertTrue(fixed.field().radius().max() >= fixed.field().radius().min());
        assertTrue(fixed.field().pipes().max() >= fixed.field().pipes().min());
        assertTrue(fixed.pipe().diameter().max() >= fixed.pipe().diameter().min());
        assertTrue(fixed.pipe().rootY() < fixed.pipe().craterBaseY());
        assertTrue(fixed.pipe().bottomY() < fixed.pipe().rootY());
        assertTrue(fixed.crater().depth().max() >= fixed.crater().depth().min());
        assertEquals(1.0, fixed.grade().barrenShare() + fixed.grade().poorShare(), 1e-9);
    }
}
