package de.wirezat.realisticores.ore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Decides which deposits and minerals are active for a given ore selection. */
public final class DepositSetup {
    private DepositSetup() {
    }

    /**
     * Checks whether a deposit is active.
     *
     * @param deposit the deposit
     * @param enabledOres ids of the ores switched on in the config
     * @return true if at least one of the deposit's ores is enabled
     */
    public static boolean isActive(Deposit deposit, Set<String> enabledOres) {
        return deposit.minerals().stream().anyMatch(mineral -> enabledOres.contains(mineral.ore()));
    }

    /**
     * Lists the enabled minerals of a deposit with the block each one places.
     *
     * @param deposit the deposit
     * @param registry source of the default blocks
     * @param enabledOres ids of the ores switched on in the config
     * @param overrides block ids by ore id that replace the default blocks
     * @return enabled minerals; ores unknown to the registry are skipped
     */
    public static List<ActiveMineral> activeMinerals(Deposit deposit, DepositRegistry registry,
            Set<String> enabledOres, Map<String, String> overrides) {
        List<ActiveMineral> active = new ArrayList<>();
        for (Mineral mineral : deposit.minerals()) {
            OreDefinition ore = registry.ores().get(mineral.ore());
            if (ore == null || !enabledOres.contains(mineral.ore())) {
                continue;
            }
            String block = overrides.getOrDefault(mineral.ore(), ore.block());
            active.add(new ActiveMineral(mineral.ore(), mineral.zone(), mineral.fraction(), block));
        }
        return List.copyOf(active);
    }
}
