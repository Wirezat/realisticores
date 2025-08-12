package wirezat.realisticores;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
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

    public static Block KIMBERLITE;
    public static Block KIMBERLITE_DIAMOND_ORE;
    public static Block CUBIC_PRESS_BLOCK;
    public static BlockEntityType<CubicPressBlockEntity> CUBIC_PRESS_BLOCK_ENTITY;
    public static ScreenHandlerType<CubicPressScreenHandler> CUBIC_PRESS_SCREEN_HANDLER_TYPE;

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(RealisticOres.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(RealisticOres.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        RealisticOres.LOGGER.info("Registering blocks for " + RealisticOres.MOD_ID);

        KIMBERLITE = registerBlock("kimberlite",
                new Block(FabricBlockSettings.create()
                        .strength(2.0f, 8.0f)
                        .requiresTool()));

        KIMBERLITE_DIAMOND_ORE = registerBlock("kimberlite_diamond_ore",
                new Block(FabricBlockSettings.create()
                        .strength(50.0f, 1200.0f)
                        .requiresTool()));

        CUBIC_PRESS_BLOCK = registerBlock("cubic_press",
                new CubicPress(FabricBlockSettings.create()
                        .strength(4.0f)
                        .requiresTool()));

        // Registrierung des Block-Entity-Typs
        CUBIC_PRESS_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(RealisticOres.MOD_ID, "cubic_press_entity"),
                FabricBlockEntityTypeBuilder.create(CubicPressBlockEntity::new, CUBIC_PRESS_BLOCK).build()
        );

        // ScreenHandler mit FABRIC API registrieren
        CUBIC_PRESS_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(
                new Identifier(RealisticOres.MOD_ID, "cubic_press_screen"),
                (syncId, inventory, buf) -> new CubicPressScreenHandler(syncId, inventory, buf)
        );
    }
}