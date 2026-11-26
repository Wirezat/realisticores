package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.indicator.GuidePoint;
import de.wirezat.realisticores.worldgen.Climate;
import de.wirezat.realisticores.worldgen.TerrainProbe;
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
        Pipe pipe = Pipe.of(0L, 0, 0, 70);
        PlumePath path = Plume.trace(pipe, Math.PI, slopeEast(Climate.TEMPERATE), 0, params);
        assertTrue(path.xs()[path.xs().length - 1] > 500.0);
        assertEquals(0.0, path.zs()[path.zs().length - 1], 50.0);
        double pyropeDownhill = Plume.density(path, 300, 0, params, params.lead());
        double ilmeniteDownhill = Plume.density(path, 300, 0, params, params.indicators().get(2));
        double pyropeUphill = Plume.density(path, -300, 0, params, params.lead());
        assertTrue(pyropeDownhill > ilmeniteDownhill);
        assertTrue(pyropeDownhill > pyropeUphill * 100.0);
    }

    @Test
    void coldClimateStretchesTheTrailAndOceanShortensIt() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        Pipe pipe = Pipe.of(0L, 0, 0, 70);
        double temperate = Plume.density(Plume.trace(pipe, 0.0, slopeEast(Climate.TEMPERATE), 0, params),
                600, 0, params, params.lead());
        double cold = Plume.density(Plume.trace(pipe, 0.0, slopeEast(Climate.COLD), 0, params),
                600, 0, params, params.lead());
        PlumePath ocean = Plume.trace(pipe, 0.0, slopeEast(Climate.OCEAN), 0, params);
        assertTrue(cold > temperate * 2.0);
        assertTrue(Plume.density(ocean, 600, 0, params, params.lead()) < temperate);
        assertEquals(params.lead().peak(), Plume.density(ocean, 0, 0, params, params.lead()), 1e-12);
    }

    @Test
    void guideLeadsFromThePannedSpotBackToThePipe() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PlumePath path = Plume.trace(Pipe.of(0L, 0, 0, 70), 0.0, slopeEast(Climate.TEMPERATE), 0, params);
        List<GuidePoint> guide = Plume.guide(path, 300, 0, 400, 10);
        assertTrue(guide.size() > 20);
        assertTrue(guide.get(0).x() > 250.0);
        assertTrue(guide.get(guide.size() - 1).x() < 20.0);
        assertTrue(guide.get(5).dirX() < -0.9);
        List<GuidePoint> offTrail = Plume.guide(path, 300, 40, 400, 10);
        assertEquals(40.0, offTrail.get(0).z(), 1.0);
        assertEquals(300.0, offTrail.get(0).x(), 1.0);
        assertTrue(offTrail.get(offTrail.size() - 1).x() < 20.0);
    }

    @Test
    void trailRunsOnOverShallowSeaFloorAndEndsInDeepWater() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PlumePath path = Plume.trace(Pipe.of(0L, 0, 0, 70), 0.0, shelf(), 63, params);
        double end = path.xs()[path.xs().length - 1];
        assertTrue(end > 200.0);
        assertTrue(end < 1000.0);
    }

    @Test
    void mapSpotsKeepTheirDistanceFromEveryPipeAndStayOnLand() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        Pipe source = Pipe.of(0L, -1200, 0, 70);
        Pipe neighbour = Pipe.of(0L, -500, 0, 70);
        PlumePath path = Plume.trace(source, 0.0, shelf(), 63, params);
        List<Plume.Spot> spots = Plume.mapSpots(path, List.of(source, neighbour), 250.0, shelf(), 63, params);
        assertFalse(spots.isEmpty());
        for (Plume.Spot spot : spots) {
            assertTrue(Math.hypot(spot.x() - source.x(), spot.z()) >= 285.0);
            assertTrue(Math.hypot(spot.x() - neighbour.x(), spot.z()) >= 285.0);
            assertTrue(shelf().terrainHeight((int) Math.round(spot.x()), 0) >= 62);
            assertTrue(Plume.density(path, spot.x(), spot.z(), params, params.lead()) >= 0.02);
        }
    }

    @Test
    void mapSpotsAreKeptOnlyWhereTheStrongestTrailHasDiamonds() {
        KimberliteParams.PlumeParams params = KimberliteParams.defaults().plume();
        PipeGrade grade = new PipeGrade(KimberliteParams.defaults().grade());
        Pipe barren = pipeWith(grade, true);
        Pipe graded = pipeWith(grade, false);
        PlumePath barrenTrail = straight(barren);
        PlumePath gradedTrail = straight(graded);
        Plume.Spot onBarren = new Plume.Spot(barren.x() + 300, barren.z());
        Plume.Spot onGraded = new Plume.Spot(graded.x() + 300, graded.z());
        assertEquals(List.of(onGraded), Plume.withDiamonds(List.of(onBarren, onGraded),
                List.of(barrenTrail, gradedTrail), grade, params));
    }

    private static Pipe pipeWith(PipeGrade grade, boolean barren) {
        int z = barren ? 0 : 5000;
        while ((grade.pipeFactor(Pipe.of(0L, 0, z, 70)) == 0.0) != barren) {
            z += 10;
        }
        return Pipe.of(0L, 0, z, 70);
    }

    private static PlumePath straight(Pipe pipe) {
        return new PlumePath(pipe, new double[] {pipe.x(), pipe.x() + 1600}, new double[] {pipe.z(), pipe.z()},
                new double[] {0, 1600}, 1.0);
    }
}
