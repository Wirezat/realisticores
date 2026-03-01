package wirezat.realisticores;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup REALISTIC_ORES_TAB = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(RealisticOres.MOD_ID, "realistic_ores_tab"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.realisticores.realistic_ores_tab"))
                    .icon(() -> new ItemStack(ModBlocks.KIMBERLITE_DIAMOND_ORE))
                    .entries((context, entries) -> {

                        // Machines
                        entries.add(ModBlocks.CUBIC_PRESS_BLOCK);

                        // Kimberlite
                        entries.add(ModBlocks.KIMBERLITE);
                        entries.add(ModBlocks.KIMBERLITE_STAIRS);
                        entries.add(ModBlocks.KIMBERLITE_SLAB);
                        entries.add(ModBlocks.KIMBERLITE_WALL);
                        entries.add(ModBlocks.KIMBERLITE_DIAMOND_ORE);

                        // Shale
                        entries.add(ModBlocks.SHALE);
                        entries.add(ModBlocks.SHALE_STAIRS);
                        entries.add(ModBlocks.SHALE_SLAB);
                        entries.add(ModBlocks.SHALE_WALL);

                        // Blackshale
                        entries.add(ModBlocks.BLACKSHALE);
                        entries.add(ModBlocks.BLACKSHALE_STAIRS);
                        entries.add(ModBlocks.BLACKSHALE_SLAB);

                        // Mudstone
                        entries.add(ModBlocks.MUDSTONE);
                        entries.add(ModBlocks.MUDSTONE_SLAB);

                        // Lignit & Coke
                        entries.add(ModBlocks.LIGNIT_ORE);
                        entries.add(ModBlocks.LIGNIT_BLOCK);
                        entries.add(ModItems.LIGNIT);
                        entries.add(ModItems.COKE);
                        entries.add(ModBlocks.COKE_BLOCK);

                        // Diamonds
                        entries.add(ModItems.RAW_DIAMOND);
                        entries.add(ModItems.SYNTHETIC_DIAMOND);
                        entries.add(ModItems.SYNTHETIC_DIAMOND_PICKAXE);
                    })
                    .build()
    );

    public static void registerModItemGroups() {}
}