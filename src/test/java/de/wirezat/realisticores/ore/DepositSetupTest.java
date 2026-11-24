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
                Map.of("peridot", "other:peridot_ore"), Map.of());
        assertEquals(1, active.size());
        assertEquals("other:peridot_ore", active.get(0).block());
        assertEquals(List.of("PERIDOTITE"), active.get(0).zones());
    }

    @Test
    void configuredSharesReplaceTheShippedShares() {
        List<ActiveMineral> active = DepositSetup.activeMinerals(DEPOSIT, REGISTRY, Set.of("peridot"), Map.of(),
                Map.of("peridot", 0.5));
        assertEquals(0.5, active.get(0).fraction());
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
        for (Deposit deposit : shipped.deposits().values()) {
            for (Mineral mineral : deposit.minerals()) {
                assertTrue(shipped.ores().containsKey(mineral.ore()), mineral.ore());
            }
        }
    }
}
