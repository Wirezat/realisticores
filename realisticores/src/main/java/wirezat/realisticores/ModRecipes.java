package wirezat.realisticores;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wirezat.realisticores.blocks.machines.cubicpress.CubicPressRecipe;

import static wirezat.realisticores.RealisticOres.MOD_ID;

public class ModRecipes {
    public static void register() {
        Registry.register(Registries.RECIPE_TYPE, new Identifier(MOD_ID, "cubic_press"), CubicPressRecipe.TYPE);
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "cubic_press"), CubicPressRecipe.Serializer.INSTANCE);
    }
}