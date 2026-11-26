package de.wirezat.realisticores.compat.emi;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.ModPress;
import de.wirezat.realisticores.press.PressRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** EMI plugin of the crystal press. */
@EmiEntrypoint
public class PressEmiPlugin implements EmiPlugin {
    public static final ResourceLocation CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "crystal_press");

    @Override
    public void register(EmiRegistry registry) {
        EmiStack icon = EmiStack.of(ModBlocks.CRYSTAL_PRESS.get());
        EmiRecipeCategory category = new EmiRecipeCategory(CATEGORY_ID, icon);
        registry.addCategory(category);
        registry.addWorkstation(category, icon);
        for (RecipeHolder<PressRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(ModPress.PRESS_TYPE.get())) {
            registry.addRecipe(new PressEmiRecipe(category, holder.id(), holder.value()));
        }
    }
}
