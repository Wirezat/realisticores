package de.wirezat.realisticores.ore;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Looks up blocks named by id in the config and the shipped ore data. */
public final class BlockIds {
    private BlockIds() {
    }

    /**
     * Finds a block by id.
     *
     * @param id block id such as {@code minecraft:sandstone}
     * @return the block, empty for a malformed id or a block that is not in the game
     */
    public static Optional<Block> block(String id) {
        return Optional.ofNullable(ResourceLocation.tryParse(id)).flatMap(BuiltInRegistries.BLOCK::getOptional);
    }

    /**
     * Finds the default state of a block by id.
     *
     * @param id block id such as {@code minecraft:sandstone}
     * @return the default state, empty for a malformed id or a block that is not in the game
     */
    public static Optional<BlockState> state(String id) {
        return block(id).map(Block::defaultBlockState);
    }
}
