package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FieldGeneratorTest {
    @Test
    void nearestFieldIsTheClosestCentreWithinTheSearchLimit() {
        FieldGenerator generator = new FieldGenerator(KimberliteParams.defaults());
        long seed = 99L;
        int cell = KimberliteParams.defaults().field().cellSize();
        Optional<Field> nearest = generator.nearestField(seed, 1234, -5678, 40000);
        double expected = Double.MAX_VALUE;
        for (int cellX = -20; cellX <= 20; cellX++) {
            for (int cellZ = -20; cellZ <= 20; cellZ++) {
                Optional<Field> field = generator.fieldAt(seed, cellX, cellZ).filter(f -> !f.pipes().isEmpty());
                if (field.isPresent()) {
                    expected = Math.min(expected, Math.hypot(field.get().centerX() - 1234, field.get().centerZ() + 5678));
                }
            }
        }
        assertEquals(expected, Math.hypot(nearest.orElseThrow().centerX() - 1234, nearest.get().centerZ() + 5678), 1e-9);
    }

    @Test
    void pipesNearFindsEveryPipeWhoseNoisyWallReachesTheChunk() {
        KimberliteParams params = KimberliteParams.defaults();
        FieldGenerator generator = new FieldGenerator(params);
        PipeShape shape = new PipeShape(params.pipe(), new PipeProfile(params.pipe()));
        long seed = 99L;
        Field field = generator.nearestField(seed, 1234, -5678, 40000).orElseThrow();
        for (Pipe pipe : field.pipes()) {
            int reach = (int) Math.ceil(shape.reach(pipe));
            for (int x = pipe.x() - reach; x <= pipe.x() + reach; x++) {
                for (int z = pipe.z() - reach; z <= pipe.z() + reach; z++) {
                    if (shape.normalizedDistance(shape.outline(pipe), x, 75, z) <= 1.0) {
                        int chunkX = x & ~15;
                        int chunkZ = z & ~15;
                        assertTrue(generator.pipesNear(seed, chunkX, chunkZ, chunkX + 15, chunkZ + 15).contains(pipe));
                    }
                }
            }
        }
    }
}
