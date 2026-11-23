package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CraterTest {
    private static final Crater CRATER = new Crater(KimberliteParams.defaults().crater());

    @Test
    void bowlIsDeepestAtTheAxisAndMeetsTheRimAtTheWall() {
        Pipe pipe = new Pipe(0, 0, 120);
        int rim = 80;
        int center = CRATER.floorY(pipe, 0.0, rim);
        int half = CRATER.floorY(pipe, 0.5, rim);
        assertEquals(rim - CRATER.depth(pipe), center);
        assertTrue(center < half);
        assertEquals(rim, CRATER.floorY(pipe, 1.0, rim));
    }

    @Test
    void lakeStaysBelowTheLowestRimButNotBelowSeaLevel() {
        assertEquals(79, CRATER.waterLevel(80, 63));
        assertEquals(63, CRATER.waterLevel(60, 63));
    }
}
