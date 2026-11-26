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
        return new ActiveMineral(ore, List.of(rank.name()), 1.0, false, block, true);
    }

    private static ActiveMineral replaced(String ore, Rank rank, String block) {
        return new ActiveMineral(ore, List.of(rank.name()), 1.0, false, block, false);
    }

    @Test
    void nothingActiveMeansNoBlocks() {
        assertTrue(CoalOres.blocks(List.of()).isEmpty());
    }

    @Test
    void disabledRankKeepsItsReplacementButIsNoCoal() {
        List<ActiveMineral> placed = List.of(mineral("coal", Rank.HARD_COAL, "m:coal"),
                replaced("anthracite", Rank.ANTHRACITE, "m:black_shale"));
        assertEquals(Map.of(Rank.HARD_COAL, "m:coal", Rank.ANTHRACITE, "m:black_shale"), CoalOres.blocks(placed));
        assertEquals(Set.of(Rank.HARD_COAL), CoalOres.coalRanks(placed));
    }

    @Test
    void accessoryMineralsTakeNoRank() {
        ActiveMineral pyrite = new ActiveMineral("pyrite", List.of("BLACK_SHALE"), 0.03, false, "m:pyrite_ore", true);
        assertEquals(Map.of(Rank.LIGNITE, "m:lignite"),
                CoalOres.blocks(List.of(mineral("lignite", Rank.LIGNITE, "m:lignite"), pyrite)));
        assertEquals(List.of(pyrite), CoalOres.accessories(List.of(mineral("lignite", Rank.LIGNITE, "m:lignite"), pyrite)));
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
