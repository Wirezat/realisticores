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
                entries.add(ModItems.SYNTHETIC_DIAMOND_PICKAXE);
                entries.add(ModItems.RAW_DIAMOND);
                entries.add(ModItems.SYNTHETIC_DIAMOND);
                entries.add(ModBlocks.KIMBERLITE);
                entries.add(ModBlocks.KIMBERLITE_DIAMOND_ORE);
            })
            .build()
    );

    public static void registerModItemGroups() {
        RealisticOres.LOGGER.info("Registering Item Groups for " + RealisticOres.MOD_ID);
    }
}
