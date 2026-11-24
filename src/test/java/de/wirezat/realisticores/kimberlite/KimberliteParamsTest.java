package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KimberliteParamsTest {
    @Test
    void sanitizedParamsKeepRangesOrderedAndSharesWithinOneHundredPercent() {
        KimberliteParams d = KimberliteParams.defaults();
        KimberliteParams.PipeParams p = d.pipe();
        KimberliteParams broken = new KimberliteParams(d.craton(),
                new KimberliteParams.FieldParams(3000, 0.5, 0.3, 800, 300, 30, 5, 80, 10),
                new KimberliteParams.PipeParams(70, 0.5, 300, 30, 64, 16, 1.2, 0.15, 90, 2.5, 100,
                        p.wallNoiseStrength(), p.minAspect(), p.outcropThreshold(), p.outcropWavelength(),
                        p.wallWavelengthFactor(), p.wallMinWavelength(), p.wallFineShare(), p.wallFineScale(),
                        p.wallVerticalStretch()),
                d.facies(),
                new KimberliteParams.CraterParams(0.08, 12, 4, 3, 12, 8, 3, false),
                new KimberliteParams.GradeParams(0.8, 0.5, 0.15, 40.0, 0.9), d.plume());
        KimberliteParams fixed = broken.sanitized();
        assertTrue(fixed.field().maxRadius() >= fixed.field().minRadius());
        assertTrue(fixed.field().maxPipes() >= fixed.field().minPipes());
        assertTrue(fixed.pipe().maxDiameter() >= fixed.pipe().minDiameter());
        assertTrue(fixed.pipe().rootY() < fixed.pipe().craterBaseY());
        assertTrue(fixed.pipe().bottomY() < fixed.pipe().rootY());
        assertTrue(fixed.crater().maxDepth() >= fixed.crater().minDepth());
        assertEquals(1.0, fixed.grade().barrenShare() + fixed.grade().poorShare(), 1e-9);
    }
}
