package de.wirezat.realisticores.press;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Items the press takes as material or catalyst, indexed once per set of loaded recipes. */
final class PressIngredients {
    private record Index(Collection<RecipeHolder<?>> source, List<PressRecipe> recipes, Set<Item> materials,
            Set<Item> catalysts) {
    }

    private static final Index EMPTY = new Index(List.of(), List.of(), Set.of(), Set.of());

    private static volatile Index serverIndex = EMPTY;
    private static volatile Index clientIndex = EMPTY;

    private PressIngredients() {
    }

    /**
     * Tells whether a stack is the material of any press recipe.
     *
     * @param recipes all loaded recipes as given by {@code RecipeManager.getRecipes()}; a new collection after a reload
     * @param client true for the recipes of the client, which keep an index of their own
     * @param stack the stack
     * @return true if a recipe takes it as material
     */
    static boolean isMaterial(Collection<RecipeHolder<?>> recipes, boolean client, ItemStack stack) {
        Index current = index(recipes, client);
        return current.materials().contains(stack.getItem())
                && current.recipes().stream().anyMatch(recipe -> recipe.material().test(stack));
    }

    /**
     * Tells whether a stack is the catalyst of any press recipe.
     *
     * @param recipes all loaded recipes as given by {@code RecipeManager.getRecipes()}; a new collection after a reload
     * @param client true for the recipes of the client, which keep an index of their own
     * @param stack the stack
     * @return true if a recipe takes it as catalyst
     */
    static boolean isCatalyst(Collection<RecipeHolder<?>> recipes, boolean client, ItemStack stack) {
        Index current = index(recipes, client);
        return current.catalysts().contains(stack.getItem())
                && current.recipes().stream().anyMatch(recipe -> recipe.catalyst().test(stack));
    }

    /**
     * Gives the index of the recipes, rebuilding it when they are not the ones indexed last.
     *
     * @param recipes all loaded recipes
     * @param client true for the index of the client
     * @return press recipes with the items of their materials and catalysts
     */
    private static Index index(Collection<RecipeHolder<?>> recipes, boolean client) {
        Index current = client ? clientIndex : serverIndex;
        if (current.source() == recipes) {
            return current;
        }
        List<PressRecipe> press = recipes.stream().map(RecipeHolder::value)
                .filter(PressRecipe.class::isInstance).map(PressRecipe.class::cast).toList();
        current = new Index(recipes, press, items(press, PressRecipe::material), items(press, PressRecipe::catalyst));
        if (client) {
            clientIndex = current;
        } else {
            serverIndex = current;
        }
        return current;
    }

    /**
     * Collects the items one ingredient of the press recipes matches.
     *
     * @param recipes the press recipes
     * @param ingredient the material or the catalyst of a recipe
     * @return every item the ingredient lists
     */
    private static Set<Item> items(List<PressRecipe> recipes, Function<PressRecipe, Ingredient> ingredient) {
        Set<Item> items = new HashSet<>();
        for (PressRecipe recipe : recipes) {
            for (ItemStack stack : ingredient.apply(recipe).getItems()) {
                items.add(stack.getItem());
            }
        }
        return Set.copyOf(items);
    }
}
