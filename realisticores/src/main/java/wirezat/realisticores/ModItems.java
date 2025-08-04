package wirezat.realisticores;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wirezat.realisticores.item.tools.syntheticdiamondpickaxe.SyntheticDiamondPickaxeItem;
import wirezat.realisticores.item.tools.syntheticdiamondpickaxe.SyntheticDiamondToolMaterial;

public class ModItems {

    public static Item RAW_DIAMOND;
    public static Item SYNTHETIC_DIAMOND;
    public static Item SYNTHETIC_DIAMOND_PICKAXE;

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RealisticOres.MOD_ID, name), item);
    }

    public static void registerModItems() {
        RealisticOres.LOGGER.info("Registering items for " + RealisticOres.MOD_ID);

        RAW_DIAMOND = registerItem("raw_diamond",
                new Item(new FabricItemSettings()));
        SYNTHETIC_DIAMOND = registerItem("synthetic_diamond",
                new Item(new FabricItemSettings()));

        SYNTHETIC_DIAMOND_PICKAXE = registerItem("synthetic_diamond_pickaxe",
                new SyntheticDiamondPickaxeItem(
                        SyntheticDiamondToolMaterial.INSTANCE,
                        1,
                        -2.8F,
                        new FabricItemSettings()
                )
        );
    }
}