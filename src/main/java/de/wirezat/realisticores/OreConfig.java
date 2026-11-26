package de.wirezat.realisticores;

import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.Deposit;
import de.wirezat.realisticores.ore.DepositRegistry;
import de.wirezat.realisticores.ore.DepositSetup;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.Deposits;
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
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SUPPRESSED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_ORES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ORE_OVERRIDES;
    private static final ModConfigSpec.IntValue COMMAND_SEARCH_LIMIT;
    private static final DepositRegistry REGISTRY = DepositRegistry.load();

    private static volatile Resolved cachedResolved;

    /**
     * Config values resolved against the shipped ore data.
     *
     * @param matcher natural ore generation to suppress: the configured list plus the tag of every enabled ore
     * @param active ids of the deposits with at least one enabled ore
     * @param minerals minerals of each active deposit with the block each one places, by deposit id
     */
    public record Resolved(OreMatcher matcher, Set<String> active, Map<String, List<ActiveMineral>> minerals) {
        /**
         * Tells whether a deposit generates.
         *
         * @param deposit deposit id
         * @return true if at least one of its ores is enabled
         */
        public boolean active(String deposit) {
            return active.contains(deposit);
        }

        /**
         * Gives the minerals a deposit places.
         *
         * @param deposit deposit id
         * @return enabled ores and replacements of disabled ones, empty while the deposit is inactive
         */
        public List<ActiveMineral> minerals(String deposit) {
            return minerals.getOrDefault(deposit, List.of());
        }
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
                        List.copyOf(REGISTRY.ores().keySet()), () -> "peridot", OreConfig::isValidOreId);
        ORE_OVERRIDES = builder
                .comment("Replaces the block placed for an ore, as ore=block, e.g. peridot=othermod:peridot_ore.")
                .defineListAllowEmpty("oreBlockOverrides", List.of(), () -> "peridot=othermod:peridot_ore",
                        OreConfig::isValidOverride);
        builder.comment("Settings of the operator command /locate realisticores.").push("command");
        COMMAND_SEARCH_LIMIT = builder
                .comment("Largest search radius in blocks; coal basins are searched up to 100000.")
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

    /**
     * Checks an entry of the suppressed ore list.
     *
     * @param entry config value
     * @return true for a block id or a block tag id prefixed with {@code #}
     */
    private static boolean isValidSuppression(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        return ResourceLocation.tryParse(text.startsWith("#") ? text.substring(1) : text) != null;
    }

    /**
     * Checks an ore id of the config.
     *
     * @param entry config value
     * @return true for lower case letters, digits and underscores
     */
    private static boolean isValidOreId(Object entry) {
        return entry instanceof String text && text.matches("[a-z0-9_]+");
    }

    /**
     * Checks an entry of the block override list.
     *
     * @param entry config value
     * @return true for {@code ore=block} with a valid ore id and block id
     */
    private static boolean isValidOverride(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        int split = text.indexOf('=');
        return split > 0 && isValidOreId(text.substring(0, split))
                && ResourceLocation.tryParse(text.substring(split + 1)) != null;
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
            Map<String, Map<String, Double>> fractions = new HashMap<>();
            Map<String, Map<String, String>> replacements = new HashMap<>();
            for (DepositType type : Deposits.ALL) {
                fractions.put(type.id(), type.minerals().fractions());
                replacements.put(type.id(), type.minerals().replacements());
            }
            resolved = resolve(REGISTRY, SUPPRESSED_ORES.get(), ENABLED_ORES.get(), ORE_OVERRIDES.get(), fractions,
                    replacements);
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

    /**
     * Resolves the config of all deposits against the ore data.
     *
     * @param registry the ores and deposits
     * @param suppressed configured ore generation to suppress
     * @param enabled ids of the enabled ores
     * @param overrides entries {@code ore=block} that replace an ore's block
     * @param fractions configured ore shares by deposit id, then ore id
     * @param replacements configured replacement blocks by deposit id, then ore id
     * @return the resolved setup
     */
    static Resolved resolve(DepositRegistry registry, List<? extends String> suppressed,
            List<? extends String> enabled, List<? extends String> overrides,
            Map<String, Map<String, Double>> fractions, Map<String, Map<String, String>> replacements) {
        Set<String> enabledOres = new HashSet<>(enabled);
        Map<String, String> overrideBlocks = new HashMap<>();
        for (String entry : overrides) {
            int split = entry.indexOf('=');
            overrideBlocks.put(entry.substring(0, split), entry.substring(split + 1));
        }
        List<String> suppressedEntries = new ArrayList<>(suppressed);
        enabledOres.stream()
                .filter(id -> registry.ores().containsKey(id))
                .forEach(id -> suppressedEntries.add("#" + registry.ores().get(id).tag()));
        Set<String> active = new HashSet<>();
        Map<String, List<ActiveMineral>> minerals = new HashMap<>();
        for (Deposit deposit : registry.deposits().values()) {
            if (DepositSetup.isActive(deposit, enabledOres)) {
                active.add(deposit.id());
                minerals.put(deposit.id(), DepositSetup.activeMinerals(deposit, registry, enabledOres, overrideBlocks,
                        fractions.getOrDefault(deposit.id(), Map.of()),
                        replacements.getOrDefault(deposit.id(), Map.of())));
            }
        }
        return new Resolved(new OreMatcher(suppressedEntries), Set.copyOf(active), Map.copyOf(minerals));
    }
}
