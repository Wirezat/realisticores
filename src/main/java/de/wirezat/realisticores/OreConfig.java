package de.wirezat.realisticores;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config: the list of ores whose natural generation is suppressed. */
public final class OreConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SUPPRESSED_ORES;

    private static volatile List<? extends String> cachedEntries;
    private static volatile OreMatcher cachedMatcher;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SUPPRESSED_ORES = builder
                .comment("Ore generation to suppress. Entries are block ids (minecraft:iron_ore)",
                        "or block tags prefixed with # (#c:ores). A feature is suppressed when",
                        "any of its ore targets matches an entry.")
                .defineListAllowEmpty("suppressedOres", List.of(), () -> "minecraft:iron_ore",
                        OreConfig::isValidEntry);
        SPEC = builder.build();
    }

    private OreConfig() {
    }

    private static boolean isValidEntry(Object entry) {
        if (!(entry instanceof String text)) {
            return false;
        }
        return ResourceLocation.tryParse(text.startsWith("#") ? text.substring(1) : text) != null;
    }

    /**
     * Returns the matcher for the currently configured ore list.
     *
     * @return matcher rebuilt whenever the configured list changes
     */
    public static OreMatcher matcher() {
        List<? extends String> entries = SUPPRESSED_ORES.get();
        OreMatcher matcher = cachedMatcher;
        if (matcher == null || !entries.equals(cachedEntries)) {
            matcher = new OreMatcher(entries);
            cachedMatcher = matcher;
            cachedEntries = List.copyOf(entries);
        }
        return matcher;
    }
}
