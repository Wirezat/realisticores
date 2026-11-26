package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AccessoriesTest {
    private static final CoalParams.AccessoryParams PARAMS = CoalParams.defaults().accessories();
    private static final Accessories RULES = new Accessories(PARAMS);
    private static final long BASIN = 99L;
    private static final double RADIUS = 500.0;

    private static SeamColumn at(double edge) {
        return new SeamColumn(Rank.LIGNITE, List.of(), edge, BASIN, RADIUS);
    }

    @Test
    void sandstoneIsOxidizedOutsideTheFrontAndHoldsOreJustInside() {
        int x = 10;
        int z = 20;
        double front = RULES.front(BASIN, x, z);
        double band = PARAMS.frontBand() / RADIUS;
        assertEquals(Accessories.Sand.OXIDIZED, RULES.sandstone(at(front + 0.01), x, z));
        assertEquals(Accessories.Sand.ORE_BAND, RULES.sandstone(at(front - band / 2), x, z));
        assertEquals(Accessories.Sand.REDUCED, RULES.sandstone(at(front - 2 * band), x, z));
        assertTrue(front > PARAMS.front().min() - 0.05 && front < PARAMS.front().max() + 0.05);
    }

    @Test
    void pyriteKeepsItsShareInNests() {
        int ore = 0;
        int paired = 0;
        int total = 0;
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 60; z++) {
                    total++;
                    if (RULES.pyrite(0L, 0.03, x, y, z)) {
                        ore++;
                        if (RULES.pyrite(0L, 0.03, x ^ 1, y, z) || RULES.pyrite(0L, 0.03, x, y ^ 1, z)
                                || RULES.pyrite(0L, 0.03, x, y, z ^ 1)) {
                            paired++;
                        }
                    }
                }
            }
        }
        assertEquals(0.03, (double) ore / total, 0.006);
        assertTrue(paired > ore / 2);
    }

    @Test
    void ironstoneKeepsItsShareInFlatNests() {
        int ore = 0;
        int beside = 0;
        int above = 0;
        int total = 0;
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 60; z++) {
                    total++;
                    if (RULES.ironstone(0L, 0.04, x, y, z)) {
                        ore++;
                        if (RULES.ironstone(0L, 0.04, x ^ 1, y, z) || RULES.ironstone(0L, 0.04, x, y, z ^ 1)) {
                            beside++;
                        }
                        if (RULES.ironstone(0L, 0.04, x, y ^ 1, z)) {
                            above++;
                        }
                    }
                }
            }
        }
        assertEquals(0.04, (double) ore / total, 0.007);
        assertTrue(beside > 3 * above);
    }

    @Test
    void firePatchesCoverTheExpectedShare() {
        int burning = 0;
        int total = 0;
        for (int x = 0; x < 2000; x += 2) {
            for (int z = 0; z < 2000; z += 2) {
                total++;
                if (RULES.onFire(7L, x, z)) {
                    burning++;
                }
            }
        }
        double meanArea = Math.PI * (Math.pow(PARAMS.fireRadius().max() + 0.5, 3) - Math.pow(PARAMS.fireRadius().min() - 0.5, 3))
                / (3.0 * (PARAMS.fireRadius().max() - PARAMS.fireRadius().min() + 1));
        double expected = PARAMS.fireChance() * meanArea / (PARAMS.fireCell() * PARAMS.fireCell());
        assertEquals(expected, (double) burning / total, expected * 0.35);
    }
}
