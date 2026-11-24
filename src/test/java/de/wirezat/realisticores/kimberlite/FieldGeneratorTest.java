package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
