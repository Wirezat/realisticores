package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.RealisticOres;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Wires the data generators of the mod into {@code runData}. */
@EventBusSubscriber(modid = RealisticOres.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    private DataGenerators() {
    }

    /**
     * Adds the generators for loot, recipes, tags, trim materials and models.
     *
     * @param event the data gathering event
     */
    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new Recipes(output, event.getLookupProvider()));
        ModBlockTags blockTags = generator.addProvider(event.includeServer(),
                new ModBlockTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new ModItemTags(output, event.getLookupProvider(),
                blockTags.contentsGetter(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output,
                event.getLookupProvider(),
                new RegistrySetBuilder().add(Registries.TRIM_MATERIAL, TrimMaterials::bootstrap),
                Set.of(RealisticOres.MOD_ID)));
        generator.addProvider(event.includeClient(), new BlockModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ItemModels(output, event.getExistingFileHelper()));
    }
}
