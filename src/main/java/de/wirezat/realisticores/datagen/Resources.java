package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ModItems;
import java.util.List;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

/** What the data generators know about the mod's blocks and items; a new resource is one entry here. */
final class Resources {
    private Resources() {
    }

    /** Tool tier a block needs to drop anything. */
    enum Tier {
        ANY,
        STONE,
        IRON
    }

    /** Tool that mines a block fastest. */
    enum Tool {
        PICKAXE,
        SHOVEL
    }

    /**
     * A gem or raw material with its storage block and armour trim colour.
     *
     * @param name trim material id
     * @param item the item
     * @param itemTags common tags of the item without the {@code c:} namespace, such as {@code gems/peridot}
     * @param block block of nine items
     * @param storageTag name of its {@code c:storage_blocks/} tag
     * @param tier tool tier the block needs
     * @param trimColor colour of trim names as {@code #RRGGBB}
     * @param trimIndex item model index of the trim material
     * @param darkerOn armour materials of the same colour, on which the trim uses its darker palette
     */
    record Resource(String name, ItemLike item, List<String> itemTags, DeferredBlock<? extends Block> block,
            String storageTag, Tier tier, String trimColor, float trimIndex, List<String> darkerOn) {
    }

    /**
     * An ore block that drops its mineral.
     *
     * @param block the ore block
     * @param drop item dropped without silk touch, multiplied by fortune
     * @param maxDrops most items one block drops before fortune; at least one
     * @param tags common block and item tags without the {@code c:} namespace, such as {@code ores/coal}
     * @param tier tool tier the block needs
     * @param carved true if cave carvers may cut through it
     */
    record Ore(DeferredBlock<? extends Block> block, ItemLike drop, int maxDrops, List<String> tags, Tier tier,
            boolean carved) {
    }

    /**
     * A rock, soil or machine block.
     *
     * @param block the block
     * @param tool tool that mines it
     * @param tier tool tier it needs
     * @param carved true if cave carvers may cut through it
     */
    record Rock(DeferredBlock<? extends Block> block, Tool tool, Tier tier, boolean carved) {
    }

    static final List<Resource> RESOURCES = List.of(
            new Resource("graphite", ModItems.GRAPHITE, List.of("raw_materials/graphite"), ModBlocks.GRAPHITE_BLOCK,
                    "graphite", Tier.STONE, "#77787E", 0.3F, List.of()),
            new Resource("natural_diamond", ModItems.NATURAL_DIAMOND, List.of("gems/diamond"),
                    ModBlocks.NATURAL_DIAMOND_BLOCK, "diamond", Tier.IRON, "#6EECD2", 0.8F,
                    List.of("minecraft:diamond")),
            new Resource("bort", ModItems.BORT, List.of("gems/bort"), ModBlocks.BORT_BLOCK, "bort", Tier.IRON,
                    "#B1AC9C", 0.1F, List.of()),
            new Resource("peridot", ModItems.PERIDOT, List.of("gems/peridot"), ModBlocks.PERIDOT_BLOCK, "peridot",
                    Tier.IRON, "#6FB52F", 0.7F, List.of()),
            new Resource("pyrope", ModItems.PYROPE, List.of("gems/pyrope"), ModBlocks.PYROPE_BLOCK, "pyrope",
                    Tier.IRON, "#C42A40", 0.4F, List.of()),
            new Resource("chromite", ModItems.CHROMITE, List.of("raw_materials/chromite"), ModBlocks.CHROMITE_BLOCK,
                    "raw_chromite", Tier.STONE, "#8F7672", 0.3F, List.of()),
            new Resource("ilmenite", ModItems.ILMENITE, List.of("raw_materials/ilmenite"), ModBlocks.ILMENITE_BLOCK,
                    "raw_ilmenite", Tier.STONE, "#7F95B4", 0.2F, List.of()),
            new Resource("lignite", ModItems.LIGNITE, List.of("gems/lignite_coal"), ModBlocks.LIGNITE_BLOCK,
                    "lignite_coal", Tier.ANY, "#80603E", 0.5F, List.of()),
            new Resource("anthracite", ModItems.ANTHRACITE, List.of("gems/anthracite"), ModBlocks.ANTHRACITE_BLOCK,
                    "anthracite", Tier.STONE, "#56657F", 0.3F, List.of()),
            new Resource("uranium", ModItems.RAW_URANIUM, List.of("raw_materials/uranium"),
                    ModBlocks.RAW_URANIUM_BLOCK, "raw_uranium", Tier.STONE, "#A9B82C", 0.7F, List.of()),
            new Resource("pyrite", ModItems.PYRITE, List.of(), ModBlocks.PYRITE_BLOCK, "pyrite", Tier.STONE,
                    "#BCAE55", 0.6F, List.of()),
            new Resource("sal_ammoniac", ModItems.SAL_AMMONIAC, List.of(), ModBlocks.SAL_AMMONIAC_BLOCK,
                    "sal_ammoniac", Tier.STONE, "#DDD2A6", 0.1F, List.of()),
            new Resource("siderite", ModItems.RAW_SIDERITE, List.of("raw_materials/siderite", "raw_materials/iron"),
                    ModBlocks.RAW_SIDERITE_BLOCK, "raw_siderite", Tier.STONE, "#A0683A", 0.2F, List.of()),
            new Resource("malachite", ModItems.MALACHITE, List.of("raw_materials/malachite", "raw_materials/copper"),
                    ModBlocks.MALACHITE_BLOCK, "raw_malachite", Tier.STONE, "#2E9A62", 0.7F, List.of()),
            new Resource("azurite", ModItems.AZURITE, List.of("raw_materials/azurite", "raw_materials/copper"),
                    ModBlocks.AZURITE_BLOCK, "raw_azurite", Tier.STONE, "#2D4FB8", 0.9F, List.of()),
            new Resource("chalcopyrite", ModItems.CHALCOPYRITE,
                    List.of("raw_materials/chalcopyrite", "raw_materials/copper"), ModBlocks.CHALCOPYRITE_BLOCK,
                    "raw_chalcopyrite", Tier.STONE, "#9A922E", 0.6F, List.of()),
            new Resource("molybdenite", ModItems.MOLYBDENITE, List.of("raw_materials/molybdenite"),
                    ModBlocks.MOLYBDENITE_BLOCK, "raw_molybdenite", Tier.STONE, "#868C99", 0.2F, List.of()));

    private static final List<String> IRONSTONE = List.of("ores/siderite", "ores/iron");
    private static final List<String> CHALCOPYRITE = List.of("ores/chalcopyrite", "ores/copper");

    static final List<Ore> ORES = List.of(
            new Ore(ModBlocks.PERIDOT_ORE, ModItems.PERIDOT, 1, List.of("ores/peridot"), Tier.STONE, true),
            new Ore(ModBlocks.KIMBERLITE_DIAMOND_ORE, ModItems.NATURAL_DIAMOND, 1, List.of("ores/diamond"), Tier.IRON,
                    true),
            new Ore(ModBlocks.KIMBERLITE_GRAPHITE_ORE, ModItems.GRAPHITE, 2, List.of("ores/graphite"), Tier.STONE,
                    true),
            new Ore(ModBlocks.KIMBERLITE_BORT_ORE, ModItems.BORT, 2, List.of("ores/bort"), Tier.STONE, true),
            new Ore(ModBlocks.COAL_SEAM, Items.COAL, 1, List.of("ores/coal"), Tier.ANY, true),
            new Ore(ModBlocks.LIGNITE_SEAM, ModItems.LIGNITE, 1, List.of("ores/lignite_coal"), Tier.ANY, true),
            new Ore(ModBlocks.ANTHRACITE_SEAM, ModItems.ANTHRACITE, 1, List.of("ores/anthracite"), Tier.STONE, true),
            new Ore(ModBlocks.BURNING_COAL_SEAM, Items.COAL, 1, List.of("ores/coal"), Tier.ANY, true),
            new Ore(ModBlocks.BURNING_LIGNITE_SEAM, ModItems.LIGNITE, 1, List.of("ores/lignite_coal"), Tier.ANY, true),
            new Ore(ModBlocks.SANDSTONE_URANIUM_ORE, ModItems.RAW_URANIUM, 1, List.of("ores/uranium"), Tier.IRON,
                    false),
            new Ore(ModBlocks.BLACK_SHALE_PYRITE_ORE, ModItems.PYRITE, 1, List.of("ores/pyrite"), Tier.STONE, false),
            new Ore(ModBlocks.SAL_AMMONIAC_CRUST, ModItems.SAL_AMMONIAC, 1, List.of("ores/sal_ammoniac"), Tier.ANY,
                    false),
            new Ore(ModBlocks.SHALE_IRONSTONE_ORE, ModItems.RAW_SIDERITE, 1, IRONSTONE, Tier.STONE, true),
            new Ore(ModBlocks.MUDSTONE_IRONSTONE_ORE, ModItems.RAW_SIDERITE, 1, IRONSTONE, Tier.STONE, true),
            new Ore(ModBlocks.BLACKBAND_IRONSTONE, ModItems.RAW_SIDERITE, 1, IRONSTONE, Tier.STONE, true),
            new Ore(ModBlocks.MALACHITE_ORE, ModItems.MALACHITE, 2, List.of("ores/malachite", "ores/copper"),
                    Tier.STONE, true),
            new Ore(ModBlocks.AZURITE_ORE, ModItems.AZURITE, 2, List.of("ores/azurite", "ores/copper"), Tier.STONE,
                    true),
            new Ore(ModBlocks.LEACHED_CHALCOPYRITE_ORE, ModItems.CHALCOPYRITE, 2, CHALCOPYRITE, Tier.STONE, true),
            new Ore(ModBlocks.POTASSIC_CHALCOPYRITE_ORE, ModItems.CHALCOPYRITE, 3, CHALCOPYRITE, Tier.STONE, true),
            new Ore(ModBlocks.PHYLLIC_CHALCOPYRITE_ORE, ModItems.CHALCOPYRITE, 3, CHALCOPYRITE, Tier.STONE, true),
            new Ore(ModBlocks.BRECCIA_CHALCOPYRITE_ORE, ModItems.CHALCOPYRITE, 4, CHALCOPYRITE, Tier.STONE, true),
            new Ore(ModBlocks.PHYLLIC_PYRITE_ORE, ModItems.PYRITE, 2, List.of("ores/pyrite"), Tier.STONE, true),
            new Ore(ModBlocks.MOLYBDENITE_ORE, ModItems.MOLYBDENITE, 1, List.of("ores/molybdenite"), Tier.IRON,
                    true),
            new Ore(ModBlocks.GOSSAN_GOLD_ORE, Items.GOLD_NUGGET, 6, List.of("ores/gold"), Tier.STONE, true));

    static final List<Rock> ROCKS = List.of(
            new Rock(ModBlocks.KIMBERLITE, Tool.PICKAXE, Tier.STONE, true),
            new Rock(ModBlocks.KIMBERLITE_BRECCIA, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.VOLCANICLASTIC_KIMBERLITE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.WEATHERED_KIMBERLITE, Tool.SHOVEL, Tier.ANY, true),
            new Rock(ModBlocks.SERPENTINIZED_KIMBERLITE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.PERIDOTITE, Tool.PICKAXE, Tier.STONE, true),
            new Rock(ModBlocks.FIRECLAY, Tool.SHOVEL, Tier.ANY, true),
            new Rock(ModBlocks.SHALE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.BLACK_SHALE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.MUDSTONE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.CLINKER, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.RUST_STAINED_SHALE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.PORPHYRY, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.POTASSIC_PORPHYRY, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.PHYLLIC_ROCK, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.PROPYLITE, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.GOSSAN, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.BLEACHED_PORPHYRY, Tool.PICKAXE, Tier.ANY, true),
            new Rock(ModBlocks.TOURMALINE_BRECCIA, Tool.PICKAXE, Tier.STONE, true),
            new Rock(ModBlocks.COALY_DIRT, Tool.SHOVEL, Tier.ANY, false),
            new Rock(ModBlocks.LIGNITIC_DIRT, Tool.SHOVEL, Tier.ANY, false),
            new Rock(ModBlocks.COALY_GRASS_BLOCK, Tool.SHOVEL, Tier.ANY, false),
            new Rock(ModBlocks.LIGNITIC_GRASS_BLOCK, Tool.SHOVEL, Tier.ANY, false),
            new Rock(ModBlocks.CRYSTAL_PRESS, Tool.PICKAXE, Tier.STONE, false));

    /**
     * Lists blocks whose model is one texture on all sides.
     *
     * @return the plain cube blocks
     */
    static List<DeferredBlock<? extends Block>> cubes() {
        return List.of(ModBlocks.ANTHRACITE_BLOCK, ModBlocks.BORT_BLOCK, ModBlocks.CHROMITE_BLOCK,
                ModBlocks.ILMENITE_BLOCK, ModBlocks.LIGNITE_BLOCK, ModBlocks.NATURAL_DIAMOND_BLOCK,
                ModBlocks.PERIDOT_BLOCK, ModBlocks.PYRITE_BLOCK, ModBlocks.PYROPE_BLOCK,
                ModBlocks.RAW_SIDERITE_BLOCK, ModBlocks.RAW_URANIUM_BLOCK, ModBlocks.SAL_AMMONIAC_BLOCK,
                ModBlocks.MALACHITE_BLOCK, ModBlocks.AZURITE_BLOCK, ModBlocks.CHALCOPYRITE_BLOCK,
                ModBlocks.MOLYBDENITE_BLOCK);
    }
}
