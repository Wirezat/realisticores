package wirezat.realisticores;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wirezat.realisticores.blocks.machines.cubicpress.CubicPressRecipe;

public class RealisticOres implements ModInitializer {
	public static final String MOD_ID = "realisticores";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RegistryKey<PlacedFeature> KIMBERLITE_MAGMA_PLACED_KEY = RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(MOD_ID, "kimberlite_magma_placed"));

	@Override
	public void onInitialize() {
		// Registriert den Rezept-Typ und Serializer direkt hier.
		Registry.register(Registries.RECIPE_TYPE, new Identifier(MOD_ID, "cubic_press"), CubicPressRecipe.TYPE);
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "cubic_press"), CubicPressRecipe.Serializer.INSTANCE);

		// Jetzt können Sie alle anderen Mod-Komponenten registrieren.
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModFeatures.registerModFeatures();

		// Fügt einen Event-Listener hinzu, um nach dem Laden des Servers zu überprüfen,
		// ob unsere Rezepte vorhanden sind.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			logCubicPressRecipes(server);
		});

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				KIMBERLITE_MAGMA_PLACED_KEY
		);
	}

	public static void logCubicPressRecipes(MinecraftServer server) {
		LOGGER.info("Überprüfe registrierte Cubic Press-Rezepte nach dem Serverstart...");

		// Ruft den globalen RecipeManager des Servers ab.
		var recipeManager = server.getRecipeManager();

		// Holt die Liste der Rezepte für den cubic_press Rezepttyp.
		var recipesList = recipeManager.listAllOfType(CubicPressRecipe.TYPE);
		int count = recipesList.size();

		// Überprüft die Rezepte für den cubic_press Rezepttyp.
		for (var recipeEntry : recipesList) {
			// Greift über die öffentliche getId() Methode der Rezept-Instanz auf die ID zu.
			LOGGER.info("Geladenes Cubic Press-Rezept: " + recipeEntry.getId().toString());
		}

		if (count == 0) {
			LOGGER.warn("Es wurden keine Cubic Press-Rezepte gefunden. Dies könnte ein Problem sein.");
		} else {
			LOGGER.info("Erfolgreich " + count + " Cubic Press-Rezepte geladen.");
		}
	}
}
