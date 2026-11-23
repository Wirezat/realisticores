package de.wirezat.realisticores;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/** Decides whether a configured feature places one of the configured ores. */
public final class OreMatcher {
    private final Set<Block> blocks = new HashSet<>();
    private final Set<TagKey<Block>> tags = new HashSet<>();

    /**
     * Builds a matcher from config entries.
     *
     * @param entries block ids, or block tag ids prefixed with {@code #}; unknown blocks are ignored
     */
    public OreMatcher(List<? extends String> entries) {
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                tags.add(TagKey.create(Registries.BLOCK, ResourceLocation.parse(entry.substring(1))));
            } else {
                BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(entry)).ifPresent(blocks::add);
            }
        }
    }

    /**
     * Checks a configured feature against the ore list.
     *
     * @param feature the feature about to be placed
     * @return true if it is an ore feature with at least one target block in the list
     */
    public boolean suppresses(ConfiguredFeature<?, ?> feature) {
        if (!(feature.config() instanceof OreConfiguration ore)) {
            return false;
        }
        for (OreConfiguration.TargetBlockState target : ore.targetStates) {
            if (matches(target.state)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(BlockState state) {
        if (blocks.contains(state.getBlock())) {
            return true;
        }
        for (TagKey<Block> tag : tags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
