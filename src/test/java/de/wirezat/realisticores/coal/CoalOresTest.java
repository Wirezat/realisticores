package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.ore.ActiveMineral;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CoalOresTest {
    private static ActiveMineral mineral(String ore, Rank rank, String block) {
        return new ActiveMineral(ore, List.of(rank.name()), 1.0, false, block);
    }

    @Test
    void nothingActiveMeansNoBlocks() {
        assertTrue(CoalOres.blocks(List.of()).isEmpty());
    }

    @Test
    void coreFallsBackToCoalWithoutAnthracite() {
        assertEquals(Map.of(Rank.HARD_COAL, "m:coal", Rank.ANTHRACITE, "m:coal"),
                CoalOres.blocks(List.of(mineral("coal", Rank.HARD_COAL, "m:coal"))));
    }

    @Test
    void anthraciteAloneFillsOnlyTheCore() {
        assertEquals(Map.of(Rank.ANTHRACITE, "m:anthracite"),
                CoalOres.blocks(List.of(mineral("anthracite", Rank.ANTHRACITE, "m:anthracite"))));
    }

    @Test
    void basinGeneratesOnlyWithAnOreItCanHold() {
        assertTrue(CoalOres.generates(BasinType.FOLDED, Set.of(Rank.ANTHRACITE)));
        assertFalse(CoalOres.generates(BasinType.HARD_COAL, Set.of(Rank.ANTHRACITE)));
        assertFalse(CoalOres.generates(BasinType.LIGNITE, Set.of(Rank.HARD_COAL, Rank.ANTHRACITE)));
    }

    @Test
    void everyActiveRankKeepsItsOwnBlock() {
        assertEquals(Map.of(Rank.HARD_COAL, "other:coal_ore", Rank.ANTHRACITE, "m:anthracite", Rank.LIGNITE, "m:lignite"),
                CoalOres.blocks(List.of(mineral("coal", Rank.HARD_COAL, "other:coal_ore"),
                        mineral("lignite", Rank.LIGNITE, "m:lignite"),
                        mineral("anthracite", Rank.ANTHRACITE, "m:anthracite"))));
    }
}
