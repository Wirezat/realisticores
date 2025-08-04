package wirezat.realisticores;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static Block KIMBERLITE;
    public static Block KIMBERLITE_DIAMOND_ORE;

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
        // Registrierungen hierher verschoben
        KIMBERLITE = registerBlock("kimberlite",
                new Block(FabricBlockSettings.create()
                        .strength(2.0f, 8.0f)
                        .requiresTool()));

        KIMBERLITE_DIAMOND_ORE = registerBlock("kimberlite_diamond_ore",
                new Block(FabricBlockSettings.create()
                        .strength(50.0f, 1200.0f)
                        .requiresTool()));
    }
}
