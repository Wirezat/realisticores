package de.wirezat.realisticores;

import de.wirezat.realisticores.ore.DepositRegistry;
import de.wirezat.realisticores.ore.OreDefinition;
import de.wirezat.realisticores.press.CrystalPressBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
            register("volcaniclastic_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).strength(1.0F, 3.0F));
    public static final DeferredBlock<Block> WEATHERED_KIMBERLITE =
            register("weathered_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY));
    public static final DeferredBlock<Block> SERPENTINIZED_KIMBERLITE =
            register("serpentinized_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> PERIDOTITE =
            register("peridotite", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5F, 9.0F));

    public static final DeferredBlock<Block> PERIDOT_ORE = registerOre("peridot_ore", "peridot",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5F, 9.0F));

    public static final DeferredBlock<Block> KIMBERLITE_DIAMOND_ORE = registerOre("kimberlite_diamond_ore", "diamond",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).strength(4.0F, 9.0F));

    public static final DeferredBlock<Block> KIMBERLITE_GRAPHITE_ORE = registerOre("kimberlite_graphite_ore", "graphite",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));

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
    public static final DeferredBlock<Block> LIGNITE_BLOCK = register("lignite_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK).mapColor(MapColor.COLOR_BROWN));
    public static final DeferredBlock<Block> ANTHRACITE_BLOCK =
            register("anthracite_block", BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK));

    public static final DeferredBlock<CrystalPressBlock> CRYSTAL_PRESS = registerPress();

    private ModBlocks() {
    }

    private static DeferredBlock<Block> register(String name, BlockBehaviour.Properties properties) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(name, properties);
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(name, block));
        return block;
    }

    private static DeferredBlock<CrystalPressBlock> registerPress() {
        DeferredBlock<CrystalPressBlock> block = BLOCKS.registerBlock("crystal_press", CrystalPressBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                        .lightLevel(state -> state.getValue(CrystalPressBlock.LIT) ? 13 : 0));
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem("crystal_press", block));
        return block;
    }

    private static DeferredBlock<Block> registerOre(String name, String oreId, BlockBehaviour.Properties properties) {
        OreDefinition ore = DepositRegistry.load().ores().get(oreId);
        DeferredBlock<Block> block = BLOCKS.registerBlock(name,
                props -> new DropExperienceBlock(UniformInt.of(ore.xpMin(), ore.xpMax()), props), properties);
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(name, block));
        return block;
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
}
