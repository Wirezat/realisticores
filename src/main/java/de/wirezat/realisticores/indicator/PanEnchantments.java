package de.wirezat.realisticores.indicator;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/** Reads the levels of the enchantments that change how a pan works. */
public final class PanEnchantments {
    private PanEnchantments() {
    }

    /**
     * Reads the level of an enchantment on a stack.
     *
     * @param level the world, source of the enchantment registry
     * @param key the enchantment
     * @param stack the stack to read
     * @return the level, 0 if absent
     */
    public static int level(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }
}
