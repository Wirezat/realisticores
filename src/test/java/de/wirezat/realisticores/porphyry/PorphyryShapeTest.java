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
        Body body = new Body(0, 0, 60, 1.0, 0.0, Stage.MODERATE, SURFACE_Y, 7L, 0.0, 0.0, List.of(), List.of());
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

    @Test
    void brecciaVentReplacesTheFreshShellsButNotTheWeatheredTop() {
        Body body = new Body(0, 0, 60, 1.0, 0.0, Stage.MODERATE, SURFACE_Y, 7L, 0.0, 0.0,
                List.of(new Body.Vent(0.6, 0.0, 8)), List.of());
        PorphyryShape shape = new PorphyryShape();
        assertEquals(Zone.BRECCIA, shape.zone(SEED, body, 36, 40, 0, SURFACE_Y, SURFACE_Y));
        assertEquals(Zone.GOSSAN, shape.zone(SEED, body, 36, SURFACE_Y - 1, 0, SURFACE_Y, SURFACE_Y));
    }
}
