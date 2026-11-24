package de.wirezat.realisticores.press;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Items offered to the crystal press recipe.
 *
 * @param material stack in the material slot
 * @param catalyst stack in the catalyst slot
 */
public record PressInput(ItemStack material, ItemStack catalyst) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? material : catalyst;
    }

    @Override
    public int size() {
        return 2;
    }
}
