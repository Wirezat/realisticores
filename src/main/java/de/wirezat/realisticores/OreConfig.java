package de.wirezat.realisticores;

import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.Deposit;
import de.wirezat.realisticores.ore.DepositRegistry;
import de.wirezat.realisticores.ore.DepositSetup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config: which ores this mod places, which block each uses, and which natural ores are suppressed. */
public final class OreConfig {
    public static final String KIMBERLITE_PIPE = "kimberlite_pipe";
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SUPPRESSED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ORE_OVERRIDES;
    private static final DepositRegistry REGISTRY = DepositRegistry.load();

    private static volatile List<List<? extends String>> cachedKey;
    private static volatile Resolved cachedResolved;

    /**
     * Config values resolved against the shipped ore data.
     *
     * @param matcher natural ore generation to suppress: the configured list plus the tag of every enabled ore
     * @param kimberlitePipeActive true if at least one ore of the kimberlite pipe is enabled
     * @param kimberliteMinerals enabled minerals of the kimberlite pipe with the block each one places
     */
    public record Resolved(OreMatcher matcher, boolean kimberlitePipeActive, List<ActiveMineral> kimberliteMinerals) {
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SUPPRESSED_ORES = builder
                .comment("Ore generation to suppress. Entries are block ids (minecraft:iron_ore)",
                        "or block tags prefixed with # (#c:ores). A feature is suppressed when",
                        "any of its ore targets matches an entry. The tag of every enabled ore",
                        "(e.g. #c:ores/peridot) is suppressed automatically.")
                .defineListAllowEmpty("suppressedOres", List.of(), () -> "minecraft:iron_ore",
                        OreConfig::isValidSuppression);
        ENABLED_ORES = builder
                .comment("Ores this mod places in its deposits. A deposit generates as soon as at least one of",
                        "its ores is enabled; with none enabled there are no kimberlite pipes at all.")
                .defineListAllowEmpty("enabledOres", List.of("peridot"), () -> "peridot", OreConfig::isValidOreId);
        ORE_OVERRIDES = builder
                .comment("Replaces the block placed for an ore, as ore=block, e.g. peridot=othermod:peridot_ore.")
                .defineListAllowEmpty("oreBlockOverrides", List.of(), () -> "peridot=othermod:peridot_ore",
                        OreConfig::isValidOverride);
        SPEC = builder.build();
    }

    private OreConfig() {
    }

    private static boolean isValidSuppression(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        return ResourceLocation.tryParse(text.startsWith("#") ? text.substring(1) : text) != null;
    }

    private static boolean isValidOreId(Object entry) {
        return entry instanceof String text && text.matches("[a-z0-9_]+");
    }

    private static boolean isValidOverride(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        int split = text.indexOf('=');
        return split > 0 && isValidOreId(text.substring(0, split))
                && ResourceLocation.tryParse(text.substring(split + 1)) != null;
    }

    /**
     * Resolves the current config values.
     *
     * @return the resolved setup, rebuilt whenever a config list changes
     */
    public static Resolved resolved() {
        List<List<? extends String>> key = List.of(List.copyOf(SUPPRESSED_ORES.get()),
                List.copyOf(ENABLED_ORES.get()), List.copyOf(ORE_OVERRIDES.get()));
        Resolved resolved = cachedResolved;
        if (resolved == null || !key.equals(cachedKey)) {
            resolved = resolve(key.get(0), key.get(1), key.get(2));
            cachedResolved = resolved;
            cachedKey = key;
        }
        return resolved;
    }

    /**
     * Returns the matcher of ores whose natural generation is suppressed.
     *
     * @return matcher for the current config
     */
    public static OreMatcher matcher() {
        return resolved().matcher();
    }

    private static Resolved resolve(List<? extends String> suppressed, List<? extends String> enabled,
            List<? extends String> overrides) {
        Set<String> enabledOres = new HashSet<>(enabled);
        Map<String, String> overrideBlocks = new HashMap<>();
        for (String entry : overrides) {
            int split = entry.indexOf('=');
            overrideBlocks.put(entry.substring(0, split), entry.substring(split + 1));
        }
        List<String> suppressedEntries = new ArrayList<>(suppressed);
        enabledOres.stream()
                .filter(id -> REGISTRY.ores().containsKey(id))
                .forEach(id -> suppressedEntries.add("#" + REGISTRY.ores().get(id).tag()));
        Deposit pipe = REGISTRY.deposits().get(KIMBERLITE_PIPE);
        return new Resolved(new OreMatcher(suppressedEntries), DepositSetup.isActive(pipe, enabledOres),
                DepositSetup.activeMinerals(pipe, REGISTRY, enabledOres, overrideBlocks));
    }
}
