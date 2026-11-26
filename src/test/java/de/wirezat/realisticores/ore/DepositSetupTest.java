package de.wirezat.realisticores.ore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DepositSetupTest {
    private static final Deposit DEPOSIT = new Deposit("pipe",
            List.of(new Mineral("diamond", List.of("BRECCIA"), 0.01, true),
                    new Mineral("peridot", List.of("PERIDOTITE"), 0.1, false)));
    private static final DepositRegistry REGISTRY = new DepositRegistry(
            Map.of("diamond", new OreDefinition("diamond", "mod:diamond_ore", "c:ores/diamond", 3, 7),
                    "peridot", new OreDefinition("peridot", "mod:peridot_ore", "c:ores/peridot", 2, 5)),
            Map.of("pipe", DEPOSIT));

    @Test
    void depositIsActiveWhenAtLeastOneOfItsOresIsEnabled() {
        assertTrue(DepositSetup.isActive(DEPOSIT, Set.of("peridot")));
        assertFalse(DepositSetup.isActive(DEPOSIT, Set.of("graphite")));
    }

    @Test
    void onlyEnabledOresArePlacedAndOverridesReplaceTheDefaultBlock() {
        List<ActiveMineral> active = DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("peridot"),
                Map.of("peridot", "other:peridot_ore"), Map.of(), Map.of());
        assertEquals(1, active.size());
        assertEquals("other:peridot_ore", active.get(0).block());
        assertEquals(List.of("PERIDOTITE"), active.get(0).zones());
    }

    @Test
    void configuredSharesReplaceTheShippedShares() {
        List<ActiveMineral> active = DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("peridot"), Map.of(),
                Map.of("peridot", 0.5), Map.of());
        assertEquals(0.5, active.get(0).fraction());
    }

    @Test
    void disabledOreTakesItsReplacementAndAnEmptyReplacementKeepsTheRock() {
        List<ActiveMineral> placed = DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("peridot"), Map.of(),
                Map.of(), Map.of("diamond", "mod:breccia", "peridot", "mod:other"));
        assertEquals(2, placed.size());
        assertEquals("mod:breccia", placed.get(0).block());
        assertFalse(placed.get(0).enabled());
        assertTrue(placed.get(1).enabled());
        assertEquals(1, DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("peridot"), Map.of(), Map.of(),
                Map.of("diamond", "")).size());
    }

    @Test
    void depositWithoutEnabledOrePlacesNothingEvenWithReplacements() {
        assertTrue(DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("graphite"), Map.of(), Map.of(),
                Map.of("diamond", "mod:breccia")).isEmpty());
    }

    @Test
    void zoneBlocksApplyOnlyWhileTheOreIsEnabledWithoutOverride() {
        Deposit deposit = new Deposit("basin", List.of(new Mineral("siderite", List.of("SHALE", "MUDSTONE"), 0.04,
                false, Map.of("MUDSTONE", "mod:mudstone_ore"))));
        DepositRegistry registry = new DepositRegistry(
                Map.of("siderite", new OreDefinition("siderite", "mod:shale_ore", "c:ores/siderite", 0, 0)),
                Map.of("basin", deposit));
        ActiveMineral on = DepositSetup.activeMinerals(deposit, registry, Set.of("siderite"), Map.of(), Map.of(),
                Map.of()).get(0);
        assertEquals("mod:shale_ore", on.blockFor("SHALE"));
        assertEquals("mod:mudstone_ore", on.blockFor("MUDSTONE"));
        ActiveMineral overridden = DepositSetup.activeMinerals(deposit, registry, Set.of("siderite"),
                Map.of("siderite", "other:iron_ore"), Map.of(), Map.of()).get(0);
        assertEquals("other:iron_ore", overridden.blockFor("MUDSTONE"));
    }

    @Test
    void shippedOresHaveAValidExperienceRange() {
        for (OreDefinition ore : DepositRegistry.load().ores().values()) {
            assertTrue(ore.xpMin() >= 0 && ore.xpMin() <= ore.xpMax(), ore.id());
        }
    }

    @Test
    void shippedDataDefinesEveryOreItsDepositsUse() {
        DepositRegistry shipped = DepositRegistry.load();
        assertTrue(shipped.deposits().containsKey("kimberlite_pipe"));
        assertTrue(shipped.deposits().containsKey("coal_basin"));
        for (Deposit deposit : shipped.deposits().values()) {
            for (Mineral mineral : deposit.minerals()) {
                assertTrue(shipped.ores().containsKey(mineral.ore()), mineral.ore());
            }
        }
    }
}
