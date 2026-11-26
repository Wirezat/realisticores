package de.wirezat.realisticores.config;

import de.wirezat.realisticores.ore.Deposit;
import de.wirezat.realisticores.ore.Mineral;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The {@code [minerals]} section of a deposit config: share of each ore and the block placed while it is off. */
public final class MineralConfig {
    private final Map<String, Supplier<Double>> fractions = new LinkedHashMap<>();
    private final Map<String, Supplier<String>> replacements = new LinkedHashMap<>();

    private MineralConfig() {
    }

    /**
     * Defines the section for the minerals of a deposit.
     *
     * @param builder builder positioned at the top level of the deposit config
     * @param deposit the deposit
     * @param hasFraction tells which minerals get a configurable share; the others fill their zone completely
     * @param defaultReplacements default replacement block id by ore id; missing ores default to keeping the rock
     * @return readers of the section
     */
    public static MineralConfig define(ModConfigSpec.Builder builder, Deposit deposit, Predicate<Mineral> hasFraction,
            Map<String, String> defaultReplacements) {
        MineralConfig config = new MineralConfig();
        builder.comment("Share of each mineral and the block placed while its ore is switched off.").push("minerals");
        for (Mineral mineral : deposit.minerals()) {
            String key = ConfigValues.camelCase(mineral.ore());
            if (hasFraction.test(mineral)) {
                config.fractions.put(mineral.ore(), ConfigValues.decimal(builder, key + "Fraction", mineral.fraction(),
                        0.0, 1.0, "Share of rock blocks in " + mineral.zones() + " that become " + mineral.ore()
                                + " ore."));
            }
            ModConfigSpec.ConfigValue<String> replacement = builder.comment("Block placed instead of " + mineral.ore()
                    + " ore while " + mineral.ore() + " is not in enabledOres; empty keeps the rock of the zone.")
                    .define(key + "Replacement", defaultReplacements.getOrDefault(mineral.ore(), ""),
                            MineralConfig::isValidReplacement);
            config.replacements.put(mineral.ore(), replacement::get);
        }
        builder.pop();
        return config;
    }

    /**
     * Checks a replacement entry.
     *
     * @param entry config value
     * @return true for an empty string or a block id
     */
    private static boolean isValidReplacement(Object entry) {
        return entry instanceof String text && (text.isEmpty() || ResourceLocation.tryParse(text) != null);
    }

    /**
     * Reads the configured shares.
     *
     * @return share by ore id, only for minerals with a configurable share
     */
    public Map<String, Double> fractions() {
        Map<String, Double> values = new HashMap<>();
        fractions.forEach((ore, value) -> values.put(ore, value.get()));
        return values;
    }

    /**
     * Reads the blocks placed instead of disabled ores.
     *
     * @return block id by ore id, empty to keep the rock
     */
    public Map<String, String> replacements() {
        Map<String, String> values = new HashMap<>();
        replacements.forEach((ore, value) -> values.put(ore, value.get()));
        return values;
    }
}
