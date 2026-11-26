package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.indicator.ModPan;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Block tags of the mod: common ore and storage tags, tool requirements, soils and carvable rocks. */
final class ModBlockTags extends BlockTagsProvider {
    /**
     * Creates the provider.
     *
     * @param output target pack
     * @param registries registries of the generated pack
     * @param files helper that checks referenced files
     */
    ModBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper files) {
        super(output, registries, RealisticOres.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (Resources.Resource resource : Resources.RESOURCES) {
            TagKey<Block> storage = common("storage_blocks/" + resource.storageTag());
            tag(storage).add(resource.block().get());
            tag(Tags.Blocks.STORAGE_BLOCKS).addTag(storage);
            mining(resource.block(), Resources.Tool.PICKAXE, resource.tier(), false);
        }
        for (Resources.Ore ore : Resources.ORES) {
            ore.tags().forEach(name -> tag(common(name)).add(ore.block().get()));
            if (ore.tags().contains("ores/iron")) {
                tag(BlockTags.IRON_ORES).add(ore.block().get());
            }
            mining(ore.block(), Resources.Tool.PICKAXE, ore.tier(), ore.carved());
        }
        Resources.ORES.stream().flatMap(ore -> ore.tags().stream()).distinct()
                .forEach(name -> tag(Tags.Blocks.ORES).addTag(common(name)));
        for (Resources.Rock rock : Resources.ROCKS) {
            mining(rock.block(), rock.tool(), rock.tier(), rock.carved());
        }
        tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.NATURAL_DIAMOND_BLOCK.get());
        List<Block> soils = List.of(ModBlocks.COALY_DIRT.get(), ModBlocks.LIGNITIC_DIRT.get(),
                ModBlocks.COALY_GRASS_BLOCK.get(), ModBlocks.LIGNITIC_GRASS_BLOCK.get());
        List<Block> grass = List.of(ModBlocks.COALY_GRASS_BLOCK.get(), ModBlocks.LIGNITIC_GRASS_BLOCK.get());
        soils.forEach(block -> {
            tag(BlockTags.DIRT).add(block);
            tag(BlockTags.SNIFFER_DIGGABLE_BLOCK).add(block);
        });
        for (TagKey<Block> spawn : List.of(BlockTags.ANIMALS_SPAWNABLE_ON,
                BlockTags.FOXES_SPAWNABLE_ON, BlockTags.FROGS_SPAWNABLE_ON,
                BlockTags.PARROTS_SPAWNABLE_ON, BlockTags.RABBITS_SPAWNABLE_ON,
                BlockTags.VALID_SPAWN, BlockTags.WOLVES_SPAWNABLE_ON)) {
            grass.forEach(block -> tag(spawn).add(block));
        }
        tag(ModPan.PANNABLE).addTag(Tags.Blocks.SANDS).addTag(Tags.Blocks.GRAVELS)
                .addTag(BlockTags.DIRT);
    }

    /**
     * Adds a block to the tags of its tool, its tool tier and, if carvable, the cave carver.
     *
     * @param block the block
     * @param tool tool that mines it
     * @param tier tool tier it needs
     * @param carved true if cave carvers may cut through it
     */
    private void mining(DeferredBlock<? extends Block> block, Resources.Tool tool, Resources.Tier tier,
            boolean carved) {
        tag(tool == Resources.Tool.PICKAXE ? BlockTags.MINEABLE_WITH_PICKAXE
                : BlockTags.MINEABLE_WITH_SHOVEL).add(block.get());
        if (tier == Resources.Tier.STONE) {
            tag(BlockTags.NEEDS_STONE_TOOL).add(block.get());
        } else if (tier == Resources.Tier.IRON) {
            tag(BlockTags.NEEDS_IRON_TOOL).add(block.get());
        }
        if (carved) {
            tag(BlockTags.OVERWORLD_CARVER_REPLACEABLES).add(block.get());
        }
    }

    /**
     * Names a common block tag.
     *
     * @param path tag path such as {@code ores/coal}
     * @return the tag {@code c:<path>}
     */
    static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
