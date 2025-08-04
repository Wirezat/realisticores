package wirezat.realisticores;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class RealisticOres implements ModInitializer {
	public static final String MOD_ID = "realisticores";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RegistryKey<PlacedFeature> KIMBERLITE_MAGMA_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(MOD_ID, "kimberlite_magma_placed"));

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModFeatures.registerModFeatures();

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				KIMBERLITE_MAGMA_PLACED_KEY
		);

		LOGGER.info("RealisticOres initialized successfully!");
	}
}