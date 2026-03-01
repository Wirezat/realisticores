package wirezat.realisticores;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import wirezat.realisticores.blocks.machines.cubicpress.CubicPress;
import wirezat.realisticores.blocks.machines.cubicpress.CubicPressBlockEntity;
import wirezat.realisticores.blocks.machines.cubicpress.CubicPressScreenHandler;

public class ModBlocks {

    // ── Sedimentary ───────────────────────────────────────────────────────────
    public static Block SHALE;
    public static Block SHALE_STAIRS;
    public static Block SHALE_SLAB;
    public static Block SHALE_WALL;

    public static Block BLACKSHALE;
    public static Block BLACKSHALE_STAIRS;
    public static Block BLACKSHALE_SLAB;

    public static Block MUDSTONE;
    public static Block MUDSTONE_SLAB;

    // ── Coal ──────────────────────────────────────────────────────────────────
    public static Block LIGNIT_ORE;
    public static Block LIGNIT_BLOCK;
    public static Block COKE_BLOCK;

    // ── Igneous / Kimberlite ──────────────────────────────────────────────────
    public static Block KIMBERLITE;
    public static Block KIMBERLITE_STAIRS;
    public static Block KIMBERLITE_SLAB;
    public static Block KIMBERLITE_WALL;
    public static Block KIMBERLITE_DIAMOND_ORE;

    // ── Machines ──────────────────────────────────────────────────────────────
    public static Block CUBIC_PRESS_BLOCK;
    public static BlockEntityType<CubicPressBlockEntity> CUBIC_PRESS_BLOCK_ENTITY;
    public static ScreenHandlerType<CubicPressScreenHandler> CUBIC_PRESS_SCREEN_HANDLER_TYPE;

    // ── Registration ──────────────────────────────────────────────────────────

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(RealisticOres.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(RealisticOres.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {

        // Shale
        SHALE = registerBlock("shale",
                new Block(FabricBlockSettings.create()
                        .strength(1.5f, 6.0f)
                        .requiresTool()));
        SHALE_STAIRS = registerBlock("shale_stairs",
                new StairsBlock(SHALE.getDefaultState(), FabricBlockSettings.copy(SHALE)));
        SHALE_SLAB = registerBlock("shale_slab",
                new SlabBlock(FabricBlockSettings.copy(SHALE)));
        SHALE_WALL = registerBlock("shale_wall",
                new WallBlock(FabricBlockSettings.copy(SHALE)));

        // Blackshale
        BLACKSHALE = registerBlock("blackshale",
                new Block(FabricBlockSettings.create()
                        .strength(1.5f, 6.0f)
                        .requiresTool()));
        BLACKSHALE_STAIRS = registerBlock("blackshale_stairs",
                new StairsBlock(BLACKSHALE.getDefaultState(), FabricBlockSettings.copy(BLACKSHALE)));
        BLACKSHALE_SLAB = registerBlock("blackshale_slab",
                new SlabBlock(FabricBlockSettings.copy(BLACKSHALE)));

        // Mudstone
        MUDSTONE = registerBlock("mudstone",
                new Block(FabricBlockSettings.create()
                        .strength(1.5f, 6.0f)
                        .requiresTool()));
        MUDSTONE_SLAB = registerBlock("mudstone_slab",
                new SlabBlock(FabricBlockSettings.copy(MUDSTONE)));

        // Lignit & Coke
        LIGNIT_ORE = registerBlock("lignit_ore",
                new Block(FabricBlockSettings.create()
                        .strength(3.0f, 3.0f)
                        .requiresTool()));
        LIGNIT_BLOCK = registerBlock("lignit_block",
                new Block(FabricBlockSettings.create()
                        .strength(5.0f, 6.0f)
                        .requiresTool()));
        COKE_BLOCK = registerBlock("coke_block",
                new Block(FabricBlockSettings.create()
                        .strength(5.0f, 6.0f)
                        .requiresTool()));

        // Kimberlite
        KIMBERLITE = registerBlock("kimberlite",
                new Block(FabricBlockSettings.create()
                        .strength(2.0f, 8.0f)
                        .requiresTool()));
        KIMBERLITE_STAIRS = registerBlock("kimberlite_stairs",
                new StairsBlock(KIMBERLITE.getDefaultState(), FabricBlockSettings.copy(KIMBERLITE)));
        KIMBERLITE_SLAB = registerBlock("kimberlite_slab",
                new SlabBlock(FabricBlockSettings.copy(KIMBERLITE)));
        KIMBERLITE_WALL = registerBlock("kimberlite_wall",
                new WallBlock(FabricBlockSettings.copy(KIMBERLITE)));
        KIMBERLITE_DIAMOND_ORE = registerBlock("kimberlite_diamond_ore",
                new Block(FabricBlockSettings.create()
                        .strength(50.0f, 1200.0f)
                        .requiresTool()));

        // Cubic Press
        CUBIC_PRESS_BLOCK = registerBlock("cubic_press",
                new CubicPress(FabricBlockSettings.create()
                        .strength(4.0f)
                        .requiresTool()));
        CUBIC_PRESS_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(RealisticOres.MOD_ID, "cubic_press_entity"),
                FabricBlockEntityTypeBuilder.create(CubicPressBlockEntity::new, CUBIC_PRESS_BLOCK).build());
        CUBIC_PRESS_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(
                new Identifier(RealisticOres.MOD_ID, "cubic_press_screen"),
                (syncId, inventory, buf) -> new CubicPressScreenHandler(syncId, inventory, buf));
    }
}