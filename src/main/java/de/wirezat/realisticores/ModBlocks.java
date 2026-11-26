package de.wirezat.realisticores;

import de.wirezat.realisticores.coal.BurningSeamBlock;
import de.wirezat.realisticores.coal.CoalPiecesBlock;
import de.wirezat.realisticores.coal.CoalyDirtBlock;
import de.wirezat.realisticores.coal.CoalyGrassBlock;
import de.wirezat.realisticores.ore.OreDefinition;
import de.wirezat.realisticores.press.CrystalPressBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's blocks and their block items. */
public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RealisticOres.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealisticOres.MOD_ID);
    private static final List<DeferredItem<? extends Item>> BLOCK_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> KIMBERLITE =
            register("kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final DeferredBlock<Block> KIMBERLITE_BRECCIA =
            register("kimberlite_breccia", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final DeferredBlock<Block> VOLCANICLASTIC_KIMBERLITE =
            register("volcaniclastic_kimberlite",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).strength(1.0F, 3.0F));
    public static final DeferredBlock<Block> WEATHERED_KIMBERLITE =
            register("weathered_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY));
    public static final DeferredBlock<Block> SERPENTINIZED_KIMBERLITE =
            register("serpentinized_kimberlite",
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> PERIDOTITE =
            register("peridotite", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5F, 9.0F));

    public static final DeferredBlock<Block> PERIDOT_ORE = registerOre("peridot_ore", "peridot",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5F, 9.0F));

    public static final DeferredBlock<Block> KIMBERLITE_DIAMOND_ORE = registerOre("kimberlite_diamond_ore", "diamond",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).strength(4.0F, 9.0F));

    public static final DeferredBlock<Block> KIMBERLITE_GRAPHITE_ORE =
            registerOre("kimberlite_graphite_ore", "graphite", BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));

    public static final DeferredBlock<Block> KIMBERLITE_BORT_ORE = registerOre("kimberlite_bort_ore", "bort",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));

    public static final DeferredBlock<Block> GRAPHITE_BLOCK =
            register("graphite_block", BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK));
    public static final DeferredBlock<Block> NATURAL_DIAMOND_BLOCK =
            register("natural_diamond_block", BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK));
    public static final DeferredBlock<Block> BORT_BLOCK = register("bort_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.STONE));
    public static final DeferredBlock<Block> PERIDOT_BLOCK = register("peridot_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN));
    public static final DeferredBlock<Block> PYROPE_BLOCK = register("pyrope_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK).mapColor(MapColor.COLOR_RED));
    public static final DeferredBlock<Block> CHROMITE_BLOCK = register("chromite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<Block> ILMENITE_BLOCK = register("ilmenite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.COLOR_GRAY));

    public static final DeferredBlock<Block> COAL_SEAM = registerOre("coal_seam", "coal",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<Block> LIGNITE_SEAM = registerOre("lignite_seam", "lignite",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_BROWN).strength(1.5F, 2.0F));
    public static final DeferredBlock<Block> ANTHRACITE_SEAM = registerOre("anthracite_seam", "anthracite",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_BLACK).strength(4.0F, 4.0F));
    public static final DeferredBlock<Block> FIRECLAY = register("fireclay",
            BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY).mapColor(MapColor.SAND));
    public static final DeferredBlock<Block> SHALE = register("shale",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_GRAY).strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> BLACK_SHALE = register("black_shale",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_BLACK).strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> MUDSTONE = register("mudstone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                    .strength(1.0F, 3.0F));
    public static final DeferredBlock<BurningSeamBlock> BURNING_COAL_SEAM =
            withItem("burning_coal_seam", BurningSeamBlock::new,
                    burning(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<BurningSeamBlock> BURNING_LIGNITE_SEAM =
            withItem("burning_lignite_seam", BurningSeamBlock::new,
                    burning(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5F, 2.0F)));
    public static final DeferredBlock<Block> CLINKER = register("clinker",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_RED).strength(2.0F, 5.0F));
    public static final DeferredBlock<Block> LIGNITE_BLOCK = register("lignite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK).mapColor(MapColor.COLOR_BROWN));
    public static final DeferredBlock<Block> ANTHRACITE_BLOCK =
            register("anthracite_block", BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK));

    public static final DeferredBlock<Block> SHALE_IRONSTONE_ORE = register("shale_ironstone_ore",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_GRAY).strength(3.0F, 3.0F));
    public static final DeferredBlock<Block> MUDSTONE_IRONSTONE_ORE = register("mudstone_ironstone_ore",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                    .strength(2.5F, 3.0F));
    public static final DeferredBlock<Block> BLACKBAND_IRONSTONE = register("blackband_ironstone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE).mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F));
    public static final DeferredBlock<Block> RUST_STAINED_SHALE = register("rust_stained_shale",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> RAW_SIDERITE_BLOCK = register("raw_siderite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN));
    public static final DeferredBlock<Block> RAW_URANIUM_BLOCK = register("raw_uranium_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<Block> PYRITE_BLOCK = register("pyrite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.GOLD));
    public static final DeferredBlock<Block> SAL_AMMONIAC_BLOCK = register("sal_ammoniac_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.CALCITE));

    public static final DeferredBlock<Block> SANDSTONE_URANIUM_ORE = registerOre("sandstone_uranium_ore", "uranium",
            BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE).strength(3.0F, 3.0F));
    public static final DeferredBlock<Block> BLACK_SHALE_PYRITE_ORE = registerOre("black_shale_pyrite_ore", "pyrite",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_BLACK).strength(1.5F, 4.0F));
    public static final DeferredBlock<Block> SAL_AMMONIAC_CRUST = registerOre("sal_ammoniac_crust", "sal_ammoniac",
            BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).mapColor(MapColor.TERRACOTTA_WHITE));

    public static final DeferredBlock<Block> PORPHYRY = register("porphyry",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY));
    public static final DeferredBlock<Block> POTASSIC_PORPHYRY = register("potassic_porphyry",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).mapColor(MapColor.TERRACOTTA_PINK));
    public static final DeferredBlock<Block> PHYLLIC_ROCK = register("phyllic_rock",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> PROPYLITE = register("propylite",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_GREEN));
    public static final DeferredBlock<Block> GOSSAN = register("gossan",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(1.0F, 3.0F));
    public static final DeferredBlock<Block> BLEACHED_PORPHYRY = register("bleached_porphyry",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_WHITE)
                    .strength(1.0F, 3.0F));
    public static final DeferredBlock<Block> TOURMALINE_BRECCIA = register("tourmaline_breccia",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_BLACK).strength(2.0F, 6.0F));

    public static final DeferredBlock<CoalPiecesBlock> COAL_PIECES =
            registerPieces("coal_pieces", MapColor.COLOR_BLACK, () -> Items.COAL);
    public static final DeferredBlock<CoalPiecesBlock> LIGNITE_PIECES =
            registerPieces("lignite_pieces", MapColor.COLOR_BROWN, ModItems.LIGNITE);
    public static final DeferredBlock<CoalPiecesBlock> ANTHRACITE_PIECES =
            registerPieces("anthracite_pieces", MapColor.COLOR_BLACK, ModItems.ANTHRACITE);

    public static final DeferredBlock<CoalyDirtBlock> COALY_DIRT = withItem("coaly_dirt", CoalyDirtBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).mapColor(MapColor.COLOR_GRAY));
    public static final DeferredBlock<CoalyDirtBlock> LIGNITIC_DIRT = withItem("lignitic_dirt", CoalyDirtBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).mapColor(MapColor.PODZOL));
    public static final DeferredBlock<CoalyGrassBlock> COALY_GRASS_BLOCK = withItem("coaly_grass_block",
            props -> new CoalyGrassBlock(COALY_DIRT, 0x626248, props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_GRAY));
    public static final DeferredBlock<CoalyGrassBlock> LIGNITIC_GRASS_BLOCK = withItem("lignitic_grass_block",
            props -> new CoalyGrassBlock(LIGNITIC_DIRT, 0x8A6C3A, props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.PODZOL));

    public static final DeferredBlock<CrystalPressBlock> CRYSTAL_PRESS = withItem("crystal_press",
            CrystalPressBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                    .lightLevel(state -> state.getValue(CrystalPressBlock.LIT) ? 13 : 0));

    private ModBlocks() {
    }

    /**
     * Registers a block with a plain block item and lists the item for the creative tab.
     *
     * @param name block id
     * @param factory creates the block from its properties
     * @param properties block properties
     * @return the registered block
     */
    private static <B extends Block> DeferredBlock<B> withItem(String name,
            Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties properties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(name, block));
        return block;
    }

    /**
     * Registers a plain block with its block item.
     *
     * @param name block id
     * @param properties block properties
     * @return the registered block
     */
    private static DeferredBlock<Block> register(String name, BlockBehaviour.Properties properties) {
        return withItem(name, Block::new, properties);
    }

    /**
     * Registers an ore block that drops the experience of its ore data, with its block item.
     *
     * @param name block id
     * @param oreId ore id in the shipped ore data
     * @param properties block properties
     * @return the registered block
     */
    private static DeferredBlock<Block> registerOre(String name, String oreId, BlockBehaviour.Properties properties) {
        OreDefinition ore = OreConfig.registry().ores().get(oreId);
        return withItem(name, props -> new DropExperienceBlock(UniformInt.of(ore.xpMin(), ore.xpMax()), props),
                properties);
    }

    /**
     * Makes a block glow like a burning seam.
     *
     * @param properties properties of the seam
     * @return the same properties, lit and emissive
     */
    private static BlockBehaviour.Properties burning(BlockBehaviour.Properties properties) {
        return properties.lightLevel(state -> 10).hasPostProcess((state, level, pos) -> true)
                .emissiveRendering((state, level, pos) -> true);
    }

    /**
     * Registers loose coal lying on the ground; it has no block item, the coal item places it.
     *
     * @param name block id
     * @param color map colour
     * @param item coal item it drops and is placed with
     * @return the registered block
     */
    private static DeferredBlock<CoalPiecesBlock> registerPieces(String name, MapColor color,
            Supplier<? extends Item> item) {
        return BLOCKS.registerBlock(name, props -> new CoalPiecesBlock(item, props),
                BlockBehaviour.Properties.of().mapColor(color).sound(SoundType.STONE).instabreak().noCollission()
                        .noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    /**
     * Registers blocks, items and the creative tab entries on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    /**
     * Gives all block items of the mod in registration order.
     *
     * @return block items for the creative tab
     */
    public static List<DeferredItem<? extends Item>> blockItems() {
        return BLOCK_ITEMS;
    }

    /**
     * Gives the blocks of loose coal.
     *
     * @return one block per coal item that can be laid down
     */
    public static List<CoalPiecesBlock> coalPieces() {
        return Stream.of(COAL_PIECES, LIGNITE_PIECES, ANTHRACITE_PIECES).map(DeferredBlock::get).toList();
    }

    /**
     * Gives the grass blocks with a fixed tint.
     *
     * @return one grass block per coaly soil
     */
    public static List<CoalyGrassBlock> coalyGrass() {
        return Stream.of(COALY_GRASS_BLOCK, LIGNITIC_GRASS_BLOCK).map(DeferredBlock::get).toList();
    }
}
