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
     * Lists the minerals an active deposit places: enabled ores with their block, disabled ores with their
     * replacement.
     *
     * @param deposit the deposit
     * @param registry source of the default blocks
     * @param enabledOres ids of the ores switched on in the config
     * @param overrides block ids by ore id that replace the default blocks
     * @param fractions ore shares by ore id that replace the shipped shares
     * @param replacements block ids by ore id placed instead of a disabled ore; empty or missing keeps the rock
     * @return minerals to place, empty if the deposit is inactive; ores unknown to the registry are skipped
     */
    public static List<ActiveMineral> activeMinerals(Deposit deposit, DepositRegistry registry,
            Set<String> enabledOres, Map<String, String> overrides, Map<String, Double> fractions,
            Map<String, String> replacements) {
        List<ActiveMineral> active = new ArrayList<>();
        if (!isActive(deposit, enabledOres)) {
            return List.of();
        }
        for (Mineral mineral : deposit.minerals()) {
            OreDefinition ore = registry.ores().get(mineral.ore());
            if (ore == null) {
                continue;
            }
            boolean enabled = enabledOres.contains(mineral.ore());
            String block = enabled ? overrides.getOrDefault(mineral.ore(), ore.block())
                    : replacements.getOrDefault(mineral.ore(), "");
            if (block.isEmpty()) {
                continue;
            }
            double fraction = fractions.getOrDefault(mineral.ore(), mineral.fraction());
            active.add(new ActiveMineral(mineral.ore(), mineral.zones(), fraction, mineral.graded(), block, enabled));
        }
        return List.copyOf(active);
    }
}
