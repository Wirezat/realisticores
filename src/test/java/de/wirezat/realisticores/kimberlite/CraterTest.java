package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CraterTest {
    private static final Crater CRATER = new Crater(KimberliteParams.defaults().crater());

    @Test
    void bowlIsDeepestAtTheAxisAndMeetsTheRimAtTheWall() {
        Pipe pipe = Pipe.of(0L, 0, 0, 120);
        int rim = 80;
        int center = CRATER.floorY(pipe, 0.0, rim, rim);
        int half = CRATER.floorY(pipe, 0.5, rim, rim);
        assertEquals(rim - CRATER.depth(pipe), center);
        assertTrue(center < half);
        assertEquals(rim, CRATER.floorY(pipe, 1.0, rim, rim));
    }

    @Test
    void wallsIntoSlopingTerrainNeverExceedTheAllowedHeight() {
        Pipe pipe = Pipe.of(0L, 0, 0, 120);
        int surface = 110;
        int allowed = Math.max(KimberliteParams.defaults().crater().maxWallHeight(), CRATER.depth(pipe));
        assertEquals(surface - allowed, CRATER.floorY(pipe, 0.9, 80, surface));
        assertEquals(60, CRATER.floorY(pipe, 0.9, 80, 60));
    }

    @Test
    void dryBiomesHoldNoLakeUnlessEnabled() {
        KimberliteParams.CraterParams d = KimberliteParams.defaults().crater();
        Crater dryOff = new Crater(d);
        Crater dryOn = new Crater(new KimberliteParams.CraterParams(d.depthPerDiameter(), d.depth(), d.floorLayers(),
                d.ringPoints(), d.maxWallHeight(), d.soilLayers(), true));
        assertTrue(dryOff.hasLake(true));
        assertTrue(!dryOff.hasLake(false));
        assertTrue(dryOn.hasLake(false));
    }

    @Test
    void lakeStaysBelowTheLowestRimButNotBelowTheSeaSurface() {
        assertEquals(79, CRATER.waterLevel(80, 63));
        assertEquals(62, CRATER.waterLevel(60, 63));
    }
}
