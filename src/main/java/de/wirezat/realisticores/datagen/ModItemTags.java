package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Item tags of the mod: common gem, raw material, ore and storage tags, trim materials, coals and pans. */
final class ModItemTags extends ItemTagsProvider {
    static final TagKey<Item> PANS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "pans"));

    /**
     * Creates the provider.
     *
     * @param output target pack
     * @param registries registries of the generated pack
     * @param blockTags block tags to copy from
     * @param files helper that checks referenced files
     */
    ModItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, registries, blockTags, RealisticOres.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (Resources.Resource resource : Resources.RESOURCES) {
            resource.itemTags().forEach(name -> tag(common(name)).add(resource.item().asItem()));
            copy(ModBlockTags.common("storage_blocks/" + resource.storageTag()),
                    common("storage_blocks/" + resource.storageTag()));
            tag(ItemTags.TRIM_MATERIALS).add(resource.item().asItem());
        }
        Resources.RESOURCES.stream().flatMap(resource -> resource.itemTags().stream()).distinct().forEach(name ->
                tag(common(name.substring(0, name.indexOf('/')))).addTag(common(name)));
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        Resources.ORES.stream().flatMap(ore -> ore.tags().stream()).distinct()
                .forEach(name -> copy(ModBlockTags.common(name), common(name)));
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(BlockTags.IRON_ORES, ItemTags.IRON_ORES);
        copy(BlockTags.DIRT, ItemTags.DIRT);
        tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.NATURAL_DIAMOND.get());
        tag(ItemTags.COALS).add(ModItems.LIGNITE.get(), ModItems.ANTHRACITE.get());
        tag(PANS).add(ModItems.IRON_PAN.get());
        tag(ItemTags.MINING_ENCHANTABLE).addTag(PANS);
        tag(ItemTags.MINING_LOOT_ENCHANTABLE).addTag(PANS);
        tag(ItemTags.VANISHING_ENCHANTABLE).addTag(PANS);
    }

    /**
     * Names a common item tag.
     *
     * @param path tag path such as {@code gems/peridot}
     * @return the tag {@code c:<path>}
     */
    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
