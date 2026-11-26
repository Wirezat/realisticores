package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.RealisticOres;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Block states and models of plain cube blocks, and the item models of all block items. */
final class BlockModels extends BlockStateProvider {
    /**
     * Creates the provider.
     *
     * @param output target pack
     * @param files helper that checks the hand-made models the item models point to
     */
    BlockModels(PackOutput output, ExistingFileHelper files) {
        super(output, RealisticOres.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        Set<Block> cubes = Resources.cubes().stream().map(DeferredBlock::get).collect(Collectors.toSet());
        cubes.forEach(block -> simpleBlockWithItem(block, cubeAll(block)));
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RealisticOres.MOD_ID))
                .filter(entry -> entry.getValue() instanceof BlockItem item && !cubes.contains(item.getBlock()))
                .forEach(entry -> simpleBlockItem(((BlockItem) entry.getValue()).getBlock(),
                        models().getExistingFile(modLoc("block/" + entry.getKey().location().getPath()))));
    }
}
