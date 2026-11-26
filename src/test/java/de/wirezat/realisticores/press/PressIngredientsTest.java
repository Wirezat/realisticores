package de.wirezat.realisticores.press;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;

class PressIngredientsTest {
    private static Collection<RecipeHolder<?>> recipes(Item material, Item catalyst) {
        PressRecipe recipe = new PressRecipe(Ingredient.of(material), 1, Ingredient.of(catalyst),
                new ItemStack(Items.DIAMOND), 100);
        return List.of(new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath("test", "press"), recipe));
    }

    @Test
    void acceptsTheItemsOfTheCurrentRecipesAfterAReload() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Collection<RecipeHolder<?>> before = recipes(Items.COAL, Items.IRON_INGOT);
        assertTrue(PressIngredients.isMaterial(before, false, new ItemStack(Items.COAL)));
        assertTrue(PressIngredients.isCatalyst(before, false, new ItemStack(Items.IRON_INGOT)));
        assertFalse(PressIngredients.isMaterial(before, false, new ItemStack(Items.IRON_INGOT)));

        Collection<RecipeHolder<?>> after = recipes(Items.CHARCOAL, Items.GOLD_INGOT);
        assertFalse(PressIngredients.isMaterial(after, false, new ItemStack(Items.COAL)));
        assertTrue(PressIngredients.isMaterial(after, false, new ItemStack(Items.CHARCOAL)));
        assertTrue(PressIngredients.isCatalyst(after, false, new ItemStack(Items.GOLD_INGOT)));
    }
}
