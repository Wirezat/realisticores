package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.RealisticOres;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/** Enchantments that change how a pan works, and the helper that reads their levels. */
public final class PanEnchantments {
    public static final ResourceKey<Enchantment> TRAIL_SENSE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "trail_sense"));

    private static final double[] TRAIL_REACH = {0.0, 48.0, 160.0};

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
                .map(stack::getEnchantmentLevel).orElse(0);
    }

    /**
     * Gives how far the particle trail of trail sense runs.
     *
     * @param level enchantment level
     * @return trail length in blocks, 0 without the enchantment
     */
    public static double trailReach(int level) {
        return TRAIL_REACH[Math.max(0, Math.min(level, TRAIL_REACH.length - 1))];
    }
}
