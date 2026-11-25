package de.wirezat.realisticores;

import de.wirezat.realisticores.coal.CoalOres;
import de.wirezat.realisticores.coal.Rank;
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
    public static final String COAL_BASIN = "coal_basin";
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SUPPRESSED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ORE_OVERRIDES;
    private static final ModConfigSpec.IntValue COMMAND_MAX_RESULTS;
    private static final ModConfigSpec.IntValue COMMAND_START_RADIUS;
    private static final ModConfigSpec.IntValue COMMAND_SEARCH_LIMIT;
    private static final DepositRegistry REGISTRY = DepositRegistry.load();

    private static volatile Resolved cachedResolved;

    /**
     * Config values resolved against the shipped ore data.
     *
     * @param matcher natural ore generation to suppress: the configured list plus the tag of every enabled ore
     * @param kimberlitePipeActive true if at least one ore of the kimberlite pipe is enabled
     * @param kimberliteMinerals enabled minerals of the kimberlite pipe with the block each one places
     * @param coalBlocks block placed for each coal rank, empty if no ore of the coal basin is enabled
     */
    public record Resolved(OreMatcher matcher, boolean kimberlitePipeActive, List<ActiveMineral> kimberliteMinerals,
            Map<Rank, String> coalBlocks) {
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
                        "its ores is enabled; with none of its ores enabled a deposit does not exist at all.")
                .defineListAllowEmpty("enabledOres",
                        List.of("diamond", "bort", "peridot", "graphite", "coal", "lignite", "anthracite"),
                        () -> "peridot", OreConfig::isValidOreId);
        ORE_OVERRIDES = builder
                .comment("Replaces the block placed for an ore, as ore=block, e.g. peridot=othermod:peridot_ore.")
                .defineListAllowEmpty("oreBlockOverrides", List.of(), () -> "peridot=othermod:peridot_ore",
                        OreConfig::isValidOverride);
        builder.comment("Settings of the operator command /realisticores pipes.").push("command");
        COMMAND_MAX_RESULTS = builder.comment("Number of nearest pipes listed.")
                .defineInRange("maxResults", 10, 1, 100);
        COMMAND_START_RADIUS = builder.comment("First search radius in blocks; it doubles until enough pipes are found.")
                .defineInRange("startRadius", 5000, 100, 100000);
        COMMAND_SEARCH_LIMIT = builder.comment("Largest search radius in blocks.")
                .defineInRange("searchLimit", 320000, 1000, 2000000);
        builder.pop();
        SPEC = builder.build();
    }

    private OreConfig() {
    }

    /**
     * Gives the ores and deposits shipped with the mod.
     *
     * @return the registry loaded at startup
     */
    public static DepositRegistry registry() {
        return REGISTRY;
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
     * Gives the number of pipes the command lists.
     *
     * @return maximum number of results
     */
    public static int commandMaxResults() {
        return COMMAND_MAX_RESULTS.get();
    }

    /**
     * Gives the first search radius of the command.
     *
     * @return radius in blocks
     */
    public static int commandStartRadius() {
        return COMMAND_START_RADIUS.get();
    }

    /**
     * Gives the largest search radius of the command.
     *
     * @return radius in blocks
     */
    public static int commandSearchLimit() {
        return COMMAND_SEARCH_LIMIT.get();
    }

    /**
     * Gives the config values resolved against the shipped ore data.
     *
     * @return the resolved setup, rebuilt after the config changed
     */
    public static Resolved resolved() {
        Resolved resolved = cachedResolved;
        if (resolved == null) {
            resolved = resolve(SUPPRESSED_ORES.get(), ENABLED_ORES.get(), ORE_OVERRIDES.get(),
                    KimberliteConfig.fractions());
            cachedResolved = resolved;
        }
        return resolved;
    }

    /** Drops the resolved values so the next use rebuilds them from the config. */
    public static void invalidate() {
        cachedResolved = null;
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
            List<? extends String> overrides, Map<String, Double> fractions) {
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
                DepositSetup.activeMinerals(pipe, REGISTRY, enabledOres, overrideBlocks, fractions),
                CoalOres.blocks(DepositSetup.activeMinerals(REGISTRY.deposits().get(COAL_BASIN), REGISTRY,
                        enabledOres, overrideBlocks, Map.of())));
    }
}
