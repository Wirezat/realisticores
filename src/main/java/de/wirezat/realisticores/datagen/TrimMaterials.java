package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.RealisticOres;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.TrimMaterial;

/** Armour trim materials, one per resource. */
final class TrimMaterials {
    private TrimMaterials() {
    }

    /**
     * Registers the trim materials into the generated pack.
     *
     * @param context registration context of the trim material registry
     */
    static void bootstrap(BootstrapContext<TrimMaterial> context) {
        for (Resources.Resource resource : Resources.RESOURCES) {
            ResourceKey<TrimMaterial> key = ResourceKey.create(Registries.TRIM_MATERIAL,
                    ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, resource.name()));
            Component description = Component.translatable(
                    "trim_material." + RealisticOres.MOD_ID + "." + resource.name())
                    .withStyle(Style.EMPTY.withColor(TextColor.parseColor(resource.trimColor()).getOrThrow()));
            String asset = RealisticOres.MOD_ID + "_" + resource.name();
            Map<Holder<ArmorMaterial>, String> darker = resource.darkerOn().stream().collect(Collectors.toMap(
                    armor -> BuiltInRegistries.ARMOR_MATERIAL.getHolderOrThrow(
                            ResourceKey.create(Registries.ARMOR_MATERIAL, ResourceLocation.parse(armor))),
                    armor -> asset + "_darker"));
            context.register(key, TrimMaterial.create(asset, resource.item().asItem(), resource.trimIndex(),
                    description, darker));
        }
    }
}
