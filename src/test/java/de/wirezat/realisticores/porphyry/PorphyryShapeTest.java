package de.wirezat.realisticores.porphyry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PorphyryShapeTest {
    private static final long SEED = 42L;
    private static final int SURFACE_Y = 110;

    @Test
    void weatheringZonesLieAboveTheFreshCoreOnTheAxis() {
        Body body = new Body(0, 0, 60, 1.0, 0.0, Stage.MODERATE, SURFACE_Y, 7L);
        PorphyryShape shape = new PorphyryShape();
        List<Zone> order = new ArrayList<>();
        for (int y = SURFACE_Y; y > 0; y--) {
            Zone zone = shape.zone(SEED, body, 0, y, 0, SURFACE_Y, SURFACE_Y);
            if (order.isEmpty() || order.get(order.size() - 1) != zone) {
                order.add(zone);
            }
        }
        assertEquals(List.of(Zone.GOSSAN, Zone.LEACHED, Zone.OXIDE, Zone.OXIDE_BASE, Zone.CEMENTATION,
                Zone.POTASSIC, Zone.CORE), order);
    }
}
