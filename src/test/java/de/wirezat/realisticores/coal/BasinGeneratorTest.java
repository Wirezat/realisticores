package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BasinGeneratorTest {
    private static final long SEED = 12345L;
    private static final int REGION = 30000;
    private static final CoalParams PARAMS = CoalParams.defaults();
    private static final BasinGenerator GENERATOR = new BasinGenerator(PARAMS);
    private static final List<Basin> BASINS = GENERATOR.basinsNear(SEED, -REGION, -REGION, REGION, REGION);

    @Test
    void sameSeedYieldsSameBasins() {
        assertFalse(BASINS.isEmpty());
        assertEquals(BASINS, new BasinGenerator(PARAMS).basinsNear(SEED, -REGION, -REGION, REGION, REGION));
    }

    @Test
    void ellipseKeepsTheRolledArea() {
        for (Basin basin : BASINS) {
            double mean = Math.sqrt(basin.longRadius() * basin.shortRadius());
            assertTrue(mean >= PARAMS.basin().radius().min() - 0.1 && mean <= PARAMS.basin().radius().max() + 0.1);
            assertTrue(basin.shortRadius() / basin.longRadius() >= 0.6 - 1e-9);
        }
    }

    @Test
    void distanceIsZeroAtCentreAndOneAtTheLongAxisEnd() {
        Basin basin = BASINS.get(0);
        assertEquals(0.0, basin.distance(basin.x(), basin.z()), 1e-9);
        assertEquals(1.0, basin.distance(basin.x() + Math.cos(basin.strike()) * basin.longRadius(),
                basin.z() + Math.sin(basin.strike()) * basin.longRadius()), 1e-9);
    }

    @Test
    void everyAreaPointOfABasinIsFound() {
        for (Basin basin : BASINS) {
            for (int dx = -1600; dx <= 1600; dx += 200) {
                for (int dz = -1600; dz <= 1600; dz += 200) {
                    int x = basin.x() + dx;
                    int z = basin.z() + dz;
                    if (basin.edgeDistance(x, z, PARAMS.basin()) < 1.0) {
                        assertTrue(GENERATOR.basinsNear(SEED, x, z, x, z).contains(basin));
                    }
                }
            }
        }
    }
}
