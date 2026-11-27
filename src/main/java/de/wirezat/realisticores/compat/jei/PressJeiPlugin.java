package de.wirezat.realisticores.compat.jei;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.kimberlite.KimberlitePipe;
import de.wirezat.realisticores.porphyry.PorphyryCopper;
import de.wirezat.realisticores.press.ModPress;
import java.util.Map;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** JEI plugin: recipes of the crystal press, the trail of each deposit and info pages of the pan and the maps. */
@JeiPlugin
public class PressJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new PressCategory(helper), new ProspectingCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PressCategory.TYPE,
                Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModPress.PRESS_TYPE.get()).stream()
                        .map(RecipeHolder::value).toList());
        Map<String, Item> maps = Map.of(KimberlitePipe.ID, ModItems.KIMBERLITE_MAP.get(),
                PorphyryCopper.ID, ModItems.PORPHYRY_MAP.get());
        registration.addRecipes(ProspectingCategory.TYPE, OreConfig.registry().deposits().values().stream()
                .filter(deposit -> !deposit.indicators().isEmpty())
                .map(deposit -> new ProspectingCategory.Recipe(deposit,
                        Optional.ofNullable(maps.get(deposit.id())).stream().toList()))
                .toList());
        registration.addIngredientInfo(ModItems.IRON_PAN.get(), Component.translatable("jei.realisticores.pan"));
        registration.addIngredientInfo(ModItems.KIMBERLITE_MAP.get(),
                Component.translatable("jei.realisticores.kimberlite_map"));
        registration.addIngredientInfo(ModItems.PORPHYRY_MAP.get(),
                Component.translatable("jei.realisticores.porphyry_map"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_PRESS.get()), PressCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.IRON_PAN.get()), ProspectingCategory.TYPE);
    }
}
