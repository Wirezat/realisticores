package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.PressRecipe;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

/** Crafting, smelting and press recipes of the mod, and the vanilla recipes it opens to all coals. */
final class Recipes extends RecipeProvider {
    private static final String IRON_GROUP = "iron_ingot";
    private static final float IRON_EXPERIENCE = 0.7F;
    private static final int SMELTING_TIME = 200;
    private static final int BLASTING_TIME = 100;
    private static final float GRAPHITE_EXPERIENCE = 0.2F;
    private static final int GRAPHITE_TIME = 800;

    /**
     * Creates the provider.
     *
     * @param output target pack
     * @param registries registries of the generated pack
     */
    Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (Resources.Resource resource : Resources.RESOURCES) {
            nineBlockStorageRecipes(output, RecipeCategory.MISC, resource.item(), RecipeCategory.BUILDING_BLOCKS,
                    resource.block(), id(getItemName(resource.block())).toString(), null,
                    id(getItemName(resource.item()) + "_from_block").toString(), null);
        }
        Ingredient ironstone = Ingredient.of(ModBlocks.SHALE_IRONSTONE_ORE, ModBlocks.MUDSTONE_IRONSTONE_ORE,
                ModBlocks.BLACKBAND_IRONSTONE);
        iron(output, ironstone, "ironstone_ore", ModBlocks.SHALE_IRONSTONE_ORE);
        iron(output, Ingredient.of(ModItems.RAW_SIDERITE), "raw_siderite", ModItems.RAW_SIDERITE);
        graphite(output, "c:storage_blocks/coal", 2, "graphite_from_coal_block");
        graphite(output, "c:storage_blocks/anthracite", 4, "graphite_from_anthracite_block");
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_PAN)
                .define('I', Tags.Items.INGOTS_IRON)
                .pattern("I I")
                .pattern(" I ")
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CRYSTAL_PRESS)
                .define('I', Items.IRON_BLOCK)
                .define('A', Items.ANVIL)
                .define('P', Items.PISTON)
                .define('B', Items.BLAST_FURNACE)
                .pattern("IAI")
                .pattern("PBP")
                .pattern("IAI")
                .unlockedBy("has_blast_furnace", has(Items.BLAST_FURNACE))
                .save(output);
        press(output, "crystal_press_graphite", ModItems.GRAPHITE, 9, Tags.Items.INGOTS_IRON, 1, 4800);
        press(output, "crystal_press_graphite_block", ModBlocks.GRAPHITE_BLOCK, 1, Tags.Items.INGOTS_IRON, 1, 4800);
        press(output, "crystal_press_graphite_chromite", ModItems.GRAPHITE, 36, tag("c:raw_materials/chromite"), 6,
                21600);
        press(output, "crystal_press_graphite_block_chromite", ModBlocks.GRAPHITE_BLOCK, 4,
                tag("c:raw_materials/chromite"), 6, 21600);
        press(output, "crystal_press_graphite_ilmenite", ModItems.GRAPHITE, 36, tag("c:raw_materials/ilmenite"), 9,
                32400);
        press(output, "crystal_press_graphite_block_ilmenite", ModBlocks.GRAPHITE_BLOCK, 4,
                tag("c:raw_materials/ilmenite"), 9, 32400);
        RecipeOutput vanilla = withoutAdvancements(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.TORCH, 4)
                .define('X', ItemTags.COALS)
                .define('#', Tags.Items.RODS_WOODEN)
                .pattern("X")
                .pattern("#")
                .unlockedBy("has_coal", has(ItemTags.COALS))
                .save(vanilla, ResourceLocation.withDefaultNamespace("torch"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.SOUL_TORCH, 4)
                .define('X', ItemTags.COALS)
                .define('#', Tags.Items.RODS_WOODEN)
                .define('S', ItemTags.SOUL_FIRE_BASE_BLOCKS)
                .pattern("X")
                .pattern("#")
                .pattern("S")
                .unlockedBy("has_coal", has(ItemTags.COALS))
                .save(vanilla, ResourceLocation.withDefaultNamespace("soul_torch"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.FIRE_CHARGE, 3)
                .requires(Items.GUNPOWDER)
                .requires(Items.BLAZE_POWDER)
                .requires(ItemTags.COALS)
                .unlockedBy("has_coal", has(ItemTags.COALS))
                .save(vanilla, ResourceLocation.withDefaultNamespace("fire_charge"));
    }

    /**
     * Adds smelting and blasting of an iron source into iron ingots.
     *
     * @param output recipe output
     * @param ingredient the iron source
     * @param name name of the source in the recipe ids
     * @param unlock item whose pickup unlocks the recipes
     */
    private static void iron(RecipeOutput output, Ingredient ingredient, String name, ItemLike unlock) {
        SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, Items.IRON_INGOT, IRON_EXPERIENCE,
                SMELTING_TIME).group(IRON_GROUP).unlockedBy(getHasName(unlock), has(unlock))
                .save(output, id("iron_ingot_from_smelting_" + name));
        SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, Items.IRON_INGOT, IRON_EXPERIENCE,
                BLASTING_TIME).group(IRON_GROUP).unlockedBy(getHasName(unlock), has(unlock))
                .save(output, id("iron_ingot_from_blasting_" + name));
    }

    /**
     * Adds blasting of a coal storage block into graphite.
     *
     * @param output recipe output
     * @param blocks tag of the storage blocks
     * @param count graphite per block
     * @param name recipe id
     */
    private static void graphite(RecipeOutput output, String blocks, int count, String name) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(tag(blocks)), RecipeCategory.MISC,
                new ItemStack(ModItems.GRAPHITE.get(), count), GRAPHITE_EXPERIENCE, GRAPHITE_TIME,
                RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new)
                .unlockedBy("has_storage_block", has(tag(blocks)))
                .save(output, id(name));
    }

    /**
     * Adds a crystal press recipe that turns graphite into diamonds.
     *
     * @param output recipe output
     * @param name recipe id
     * @param material graphite item or block
     * @param materialCount material items per run
     * @param catalyst tag of the catalyst consumed per run
     * @param diamonds diamonds per run
     * @param time duration of a run in ticks
     */
    private static void press(RecipeOutput output, String name, ItemLike material, int materialCount,
            TagKey<Item> catalyst, int diamonds, int time) {
        output.accept(id(name), new PressRecipe(Ingredient.of(material), materialCount, Ingredient.of(catalyst),
                new ItemStack(Items.DIAMOND, diamonds), time), null);
    }

    /**
     * Wraps an output so recipes replacing vanilla ones keep the vanilla advancements.
     *
     * @param output recipe output
     * @return output that drops the advancement of each recipe
     */
    private static RecipeOutput withoutAdvancements(RecipeOutput output) {
        return new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                    ICondition... conditions) {
                output.accept(id, recipe, null, conditions);
            }
        };
    }

    /**
     * Names a recipe of the mod.
     *
     * @param name recipe name
     * @return the recipe id in the mod's namespace
     */
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, name);
    }

    /**
     * Names an item tag.
     *
     * @param id tag id such as {@code c:raw_materials/chromite}
     * @return the tag key
     */
    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }
}
