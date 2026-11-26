package de.wirezat.realisticores;

import de.wirezat.realisticores.ore.BlockIds;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
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
                BlockIds.block(entry).ifPresent(blocks::add);
            }
        }
    }

    /**
     * Checks a configured feature against the ore list. Only the overworld is affected, so ores of other
     * dimensions that share a tag with a replaced ore stay.
     *
     * @param dimension the dimension the feature is placed in
     * @param feature the feature about to be placed
     * @return true in the overworld if it is an ore feature with at least one target block in the list
     */
    public boolean suppresses(ResourceKey<Level> dimension, ConfiguredFeature<?, ?> feature) {
        if (dimension != Level.OVERWORLD || !(feature.config() instanceof OreConfiguration ore)) {
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
