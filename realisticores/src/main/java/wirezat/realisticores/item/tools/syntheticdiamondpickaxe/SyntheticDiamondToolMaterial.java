package wirezat.realisticores.item.tools.syntheticdiamondpickaxe;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import wirezat.realisticores.ModItems;

public class SyntheticDiamondToolMaterial implements ToolMaterial {
    public static final SyntheticDiamondToolMaterial INSTANCE = new SyntheticDiamondToolMaterial();

    @Override
    public int getDurability() {
        return 1200;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 7.5F;
    }

    @Override
    public float getAttackDamage() {
        return 2.5F;
    }

    @Override
    public int getMiningLevel() {
        return 4;
    }

    @Override
    public int getEnchantability() {
        return 8;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.SYNTHETIC_DIAMOND);
    }
}