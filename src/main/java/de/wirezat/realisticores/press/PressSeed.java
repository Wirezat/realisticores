package de.wirezat.realisticores.press;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * What an item does as seed crystal in the press.
 *
 * @param uses number of crystals the recipes yield, counted without the seed's bonus, until the seed is worn out
 * @param speed divisor of the recipe time, below 1 to slow the press down
 * @param yield multiplier of the recipe result; fractions add up over several runs
 */
public record PressSeed(int uses, double speed, double yield) {
    public static final Codec<PressSeed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 10_000).fieldOf("uses").forGetter(PressSeed::uses),
            Codec.doubleRange(0.01, 100.0).optionalFieldOf("speed", 1.0).forGetter(PressSeed::speed),
            Codec.doubleRange(1.0, 64.0).optionalFieldOf("yield", 1.0).forGetter(PressSeed::yield))
            .apply(instance, PressSeed::new));

    /**
     * Gives what a stack does as seed crystal.
     *
     * @param stack the stack
     * @return its seed data, or null if it is no seed crystal
     */
    @Nullable
    public static PressSeed of(ItemStack stack) {
        return stack.getItemHolder().getData(ModPress.SEEDS);
    }

    /**
     * Lists all seed crystals.
     *
     * @return one stack per item
     */
    public static List<ItemStack> all() {
        return BuiltInRegistries.ITEM.getDataMap(ModPress.SEEDS).keySet().stream()
                .map(key -> new ItemStack(BuiltInRegistries.ITEM.get(key))).toList();
    }
}
