package wirezat.realisticores;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import wirezat.realisticores.blocks.machines.cubicpress.CubicPressRecipe;

public class RealisticOres implements ModInitializer {

	public static final String MOD_ID = "realisticores";

	@Override
	public void onInitialize() {

		Registry.register(Registries.RECIPE_TYPE,
				new Identifier(MOD_ID, "cubic_press"),
				CubicPressRecipe.TYPE);

		Registry.register(Registries.RECIPE_SERIALIZER,
				new Identifier(MOD_ID, "cubic_press"),
				CubicPressRecipe.Serializer.INSTANCE);

		ModBlocks.registerModBlocks();
		ModItems.registerModItems();

	}
}