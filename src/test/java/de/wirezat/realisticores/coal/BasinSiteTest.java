package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.worldgen.Climate;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.function.IntBinaryOperator;
import org.junit.jupiter.api.Test;

class BasinSiteTest {
    private static final CoalParams.ReliefParams RELIEF = CoalParams.defaults().relief();
    private static final Basin BASIN = new Basin(0, 0, 1000.0, 800.0, 0.0, BasinType.LIGNITE, 1L);

    private static BasinSite site(IntBinaryOperator height) {
        return BasinSite.measure(BASIN, new TerrainProbe() {
            @Override
            public int terrainHeight(int x, int z) {
                return height.applyAsInt(x, z);
            }

            @Override
            public Climate climate(int x, int z) {
                return Climate.TEMPERATE;
            }

            @Override
            public boolean hasPrecipitation(int x, int y, int z) {
                return true;
            }
        }, RELIEF);
    }

    @Test
    void flatGroundKeepsTheRolledType() {
        BasinSite site = site((x, z) -> 100);
        assertEquals(100, site.datum());
        assertFalse(site.mountain(RELIEF));
        assertEquals(BasinType.LIGNITE, site.type(BASIN, RELIEF));
    }

    @Test
    void highReliefInTheMiddleIsAMountain() {
        int peak = 100 + RELIEF.mountainRelief() + 1;
        BasinSite site = site((x, z) -> x == 0 && z == 0 ? peak : 100);
        assertEquals(RELIEF.mountainRelief() + 1, site.relief());
        assertTrue(site.mountain(RELIEF));
    }

    @Test
    void twoHighRimPointsMakeAFoldedBasin() {
        BasinSite site = site((x, z) -> Math.abs(x) == 1000 ? 100 + RELIEF.foothillRise() : 100);
        assertEquals(BasinType.FOLDED, site.type(BASIN, RELIEF));
    }

    @Test
    void oneHighRimPointDoesNot() {
        BasinSite site = site((x, z) -> x == 1000 ? 100 + RELIEF.foothillRise() : 100);
        assertEquals(BasinType.LIGNITE, site.type(BASIN, RELIEF));
    }
}
