package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.RealisticOres;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Flat item models of the gems and raw materials. */
final class ItemModels extends ItemModelProvider {
    /**
     * Creates the provider.
     *
     * @param output target pack
     * @param files helper that checks the textures
     */
    ItemModels(PackOutput output, ExistingFileHelper files) {
        super(output, RealisticOres.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        Resources.RESOURCES.forEach(resource -> basicItem(resource.item().asItem()));
    }
}
