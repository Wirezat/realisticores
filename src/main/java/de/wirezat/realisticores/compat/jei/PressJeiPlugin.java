package de.wirezat.realisticores.compat.jei;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.ModPress;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** JEI plugin: recipes of the crystal press and info pages of the pan and the kimberlite map. */
@JeiPlugin
public class PressJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PressCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PressCategory.TYPE,
                Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModPress.PRESS_TYPE.get()).stream()
                        .map(RecipeHolder::value).toList());
        registration.addIngredientInfo(ModItems.IRON_PAN.get(), Component.translatable("jei.realisticores.pan"));
        registration.addIngredientInfo(ModItems.KIMBERLITE_MAP.get(),
                Component.translatable("jei.realisticores.kimberlite_map"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_PRESS.get()), PressCategory.TYPE);
    }
}
