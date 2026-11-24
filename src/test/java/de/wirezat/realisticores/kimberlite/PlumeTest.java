package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void trailRunsOnOverShallowSeaFloorAndEndsInDeepWater() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        TerrainProbe shelf = new TerrainProbe() {
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
        PlumePath path = Plume.trace(new Pipe(0, 0, 70), 0.0, shelf, 63, params);
        double end = path.xs()[path.xs().length - 1];
        assertTrue(end > 200.0);
        assertTrue(end < 1000.0);
    }

    @Test
    void widthFloorKeepsANarrowTrailVisibleOnACoarseGrid() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PlumePath path = Plume.trace(new Pipe(0, 0, 10), 0.0, slopeEast(Climate.TEMPERATE), 0, params);
        double sharp = Plume.density(path, 60, 40, params, params.pyrope());
        double floored = Plume.lineDensity(path, 60, 40, params, params.pyrope(), 24.0);
        assertTrue(floored > sharp * 3.0);
    }
}
