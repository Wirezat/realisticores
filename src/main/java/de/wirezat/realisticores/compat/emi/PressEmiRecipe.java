package de.wirezat.realisticores.compat.emi;

import de.wirezat.realisticores.press.PressRecipe;
import de.wirezat.realisticores.press.PressSeed;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/** EMI view of one crystal press recipe. */
public class PressEmiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final EmiIngredient material;
    private final EmiIngredient catalyst;
    private final EmiStack result;
    private final int time;

    /**
     * Creates the view.
     *
     * @param category EMI category of the press
     * @param id recipe id
     * @param recipe the press recipe
     */
    public PressEmiRecipe(EmiRecipeCategory category, ResourceLocation id, PressRecipe recipe) {
        this.category = category;
        this.id = id;
        this.material = EmiIngredient.of(recipe.material(), recipe.materialCount());
        this.catalyst = EmiIngredient.of(recipe.catalyst());
        this.result = EmiStack.of(recipe.result());
        this.time = recipe.time();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Nullable
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(material, catalyst);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(seeds());
    }

    private static EmiIngredient seeds() {
        return EmiIngredient.of(PressSeed.all().stream().map(EmiStack::of).toList());
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(result);
    }

    @Override
    public int getDisplayWidth() {
        return 126;
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(catalyst, 0, 0);
        widgets.addSlot(material, 30, 0);
        widgets.addSlot(EmiStack.of(Items.COAL), 30, 36);
        widgets.addSlot(seeds(), 0, 36).catalyst(true);
        widgets.addTexture(EmiTexture.EMPTY_FLAME, 31, 19);
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 31, 19, 4000, false, true, true);
        widgets.addFillingArrow(53, 17, time * 50);
        widgets.addSlot(result, 90, 14).large(true).recipeContext(this);
        widgets.addText(Component.translatable("gui.realisticores.crystal_press.time", time / 20), 53, 40, 0xFF404040,
                false);
    }
}
