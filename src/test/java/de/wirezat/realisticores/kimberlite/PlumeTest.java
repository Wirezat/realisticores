package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PlumeTest {
    private static TerrainProbe slopeEast(Climate climate) {
        return new TerrainProbe() {
            @Override
            public int terrainHeight(int x, int z) {
                return 300 - (int) (x * 0.2);
            }

            @Override
            public Climate climate(int x, int z) {
                return climate;
            }

            @Override
            public boolean hasPrecipitation(int x, int y, int z) {
                return true;
            }
        };
    }

    private static TerrainProbe shelf() {
        return new TerrainProbe() {
            @Override
            public int terrainHeight(int x, int z) {
                return 60 - x / 10;
            }

            @Override
            public Climate climate(int x, int z) {
                return Climate.TEMPERATE;
            }

            @Override
            public boolean hasPrecipitation(int x, int y, int z) {
                return true;
            }
        };
    }

    @Test
    void trailRunsDownhillAndIlmeniteStopsBeforePyrope() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        Pipe pipe = new Pipe(0, 0, 70);
        PlumePath path = Plume.trace(pipe, Math.PI, slopeEast(Climate.TEMPERATE), 0, params);
        assertTrue(path.xs()[path.xs().length - 1] > 500.0);
        assertEquals(0.0, path.zs()[path.zs().length - 1], 50.0);
        double pyropeDownhill = Plume.density(path, 300, 0, params, params.pyrope());
        double ilmeniteDownhill = Plume.density(path, 300, 0, params, params.ilmenite());
        double pyropeUphill = Plume.density(path, -300, 0, params, params.pyrope());
        assertTrue(pyropeDownhill > ilmeniteDownhill);
        assertTrue(pyropeDownhill > pyropeUphill * 100.0);
    }

    @Test
    void coldClimateStretchesTheTrailAndOceanShortensIt() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        Pipe pipe = new Pipe(0, 0, 70);
        double temperate = Plume.density(Plume.trace(pipe, 0.0, slopeEast(Climate.TEMPERATE), 0, params),
                600, 0, params, params.pyrope());
        double cold = Plume.density(Plume.trace(pipe, 0.0, slopeEast(Climate.COLD), 0, params),
                600, 0, params, params.pyrope());
        PlumePath ocean = Plume.trace(pipe, 0.0, slopeEast(Climate.OCEAN), 0, params);
        assertTrue(cold > temperate * 2.0);
        assertTrue(Plume.density(ocean, 600, 0, params, params.pyrope()) < temperate);
        assertEquals(params.pyrope().peak(), Plume.density(ocean, 0, 0, params, params.pyrope()), 1e-12);
    }

    @Test
    void guideLeadsFromThePannedSpotBackToThePipe() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PlumePath path = Plume.trace(new Pipe(0, 0, 70), 0.0, slopeEast(Climate.TEMPERATE), 0, params);
        List<Plume.GuidePoint> guide = Plume.guide(path, 300, 0, 400, 10);
        assertTrue(guide.size() > 20);
        assertTrue(guide.get(0).x() > 250.0);
        assertTrue(guide.get(guide.size() - 1).x() < 20.0);
        assertTrue(guide.get(5).dirX() < -0.9);
        List<Plume.GuidePoint> offTrail = Plume.guide(path, 300, 40, 400, 10);
        assertEquals(40.0, offTrail.get(0).z(), 1.0);
        assertEquals(300.0, offTrail.get(0).x(), 1.0);
        assertTrue(offTrail.get(offTrail.size() - 1).x() < 20.0);
    }

    @Test
    void trailRunsOnOverShallowSeaFloorAndEndsInDeepWater() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PlumePath path = Plume.trace(new Pipe(0, 0, 70), 0.0, shelf(), 63, params);
        double end = path.xs()[path.xs().length - 1];
        assertTrue(end > 200.0);
        assertTrue(end < 1000.0);
    }

    @Test
    void mapSpotsKeepTheirDistanceFromEveryPipeAndStayOnLand() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        Pipe source = new Pipe(-1200, 0, 70);
        Pipe neighbour = new Pipe(-500, 0, 70);
        PlumePath path = Plume.trace(source, 0.0, shelf(), 63, params);
        List<Plume.Spot> spots = Plume.mapSpots(path, List.of(source, neighbour), shelf(), 63, params);
        assertFalse(spots.isEmpty());
        for (Plume.Spot spot : spots) {
            assertTrue(Math.hypot(spot.x() - source.x(), spot.z()) >= 285.0);
            assertTrue(Math.hypot(spot.x() - neighbour.x(), spot.z()) >= 285.0);
            assertTrue(shelf().terrainHeight((int) Math.round(spot.x()), 0) >= 62);
            assertTrue(Plume.density(path, spot.x(), spot.z(), params, params.pyrope()) >= 0.02);
        }
    }
}
