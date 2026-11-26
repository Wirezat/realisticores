package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BasinLayoutTest {
    private static final long SEED = 32L;
    private static final CoalParams PARAMS = CoalParams.defaults();

    private static BasinLayout layout(BasinType type) {
        return BasinLayout.of(new Basin(0, 0, 1000.0, 800.0, 0.3, type, SEED), type, PARAMS);
    }

    @Test
    void seamsFitBelowTheCover() {
        for (BasinType type : BasinType.values()) {
            BasinLayout layout = layout(type);
            assertFalse(layout.seams().isEmpty());
            double last = -1.0;
            for (BasinLayout.Seam seam : layout.seams()) {
                assertTrue(seam.offset() > last);
                assertTrue(seam.offset() + seam.thickness() <= layout.depth() - PARAMS.seam().cover());
                last = seam.offset();
            }
        }
    }

    @Test
    void countsStayInTheTypeRange() {
        for (BasinType type : BasinType.values()) {
            CoalParams.TypeParams p = PARAMS.type(type);
            BasinLayout layout = layout(type);
            assertTrue(layout.seams().size() <= p.seams().max());
            assertTrue(layout.faults().size() <= p.maxFaults());
        }
    }
}
