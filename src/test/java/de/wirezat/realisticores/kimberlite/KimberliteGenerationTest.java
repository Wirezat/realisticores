package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KimberliteGenerationTest {
    private static final long SEED = 12345L;
    private static final int REGION = 30000;

    private static KimberliteParams withCratonThreshold(double threshold) {
        KimberliteParams defaults = KimberliteParams.defaults();
        return new KimberliteParams(
                new KimberliteParams.CratonParams(defaults.craton().wavelength(), threshold),
                defaults.field(),
                defaults.pipe(),
                defaults.facies());
    }

    @Test
    void sameSeedYieldsSamePipes() {
        FieldGenerator generator = new FieldGenerator(withCratonThreshold(-2.0));
        List<Pipe> first = generator.pipesNear(SEED, -REGION, -REGION, REGION, REGION);
        List<Pipe> second = generator.pipesNear(SEED, -REGION, -REGION, REGION, REGION);
        assertFalse(first.isEmpty());
        assertEquals(first, second);
    }

    @Test
    void noPipesOutsideCraton() {
        FieldGenerator generator = new FieldGenerator(withCratonThreshold(2.0));
        assertTrue(generator.pipesNear(SEED, -REGION, -REGION, REGION, REGION).isEmpty());
    }

    @Test
    void pipeDiametersStayWithinConfiguredBounds() {
        KimberliteParams params = withCratonThreshold(-2.0);
        List<Pipe> pipes = new FieldGenerator(params).pipesNear(SEED, -REGION, -REGION, REGION, REGION);
        assertFalse(pipes.isEmpty());
        for (Pipe pipe : pipes) {
            assertTrue(pipe.diameter() >= params.pipe().minDiameter());
            assertTrue(pipe.diameter() <= params.pipe().maxDiameter());
        }
    }

    @Test
    void pipesInOneFieldKeepMinimumSpacing() {
        KimberliteParams params = withCratonThreshold(-2.0);
        FieldGenerator generator = new FieldGenerator(params);
        int checked = 0;
        for (int cellX = -3; cellX <= 3; cellX++) {
            for (int cellZ = -3; cellZ <= 3; cellZ++) {
                Optional<Field> field = generator.fieldAt(SEED, cellX, cellZ);
                if (field.isEmpty()) {
                    continue;
                }
                List<Pipe> pipes = field.get().pipes();
                for (int i = 0; i < pipes.size(); i++) {
                    for (int j = i + 1; j < pipes.size(); j++) {
                        double distance = Math.hypot(pipes.get(i).x() - pipes.get(j).x(),
                                pipes.get(i).z() - pipes.get(j).z());
                        assertTrue(distance >= params.field().minSpacing());
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 0);
    }

    @Test
    void radiusFollowsCraterDiatremeRootProfile() {
        KimberliteParams.PipeParams p = KimberliteParams.defaults().pipe();
        PipeProfile profile = new PipeProfile(p);
        Pipe pipe = new Pipe(0, 0, 100);
        double craterTop = profile.radiusAt(pipe, p.craterBaseY() + p.craterHeight());
        double diatremeTop = profile.radiusAt(pipe, p.craterBaseY());
        double diatremeBottom = profile.radiusAt(pipe, p.rootY());
        double rootBottom = profile.radiusAt(pipe, p.bottomY());
        assertTrue(craterTop > diatremeTop);
        assertTrue(diatremeTop > diatremeBottom);
        assertTrue(diatremeBottom > rootBottom);
        assertEquals(p.rootMinRadius(), rootBottom, 1e-9);
    }
}
