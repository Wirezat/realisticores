package de.wirezat.realisticores.press;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Recipe of the crystal press: material and a catalyst are pressed into a result.
 *
 * @param material ingredient in the material slot
 * @param materialCount number of material items one run consumes
 * @param catalyst ingredient in the catalyst slot; one item is consumed per run
 * @param result item produced by one run
 * @param time duration of one run in ticks of burning fuel
 */
public record PressRecipe(Ingredient material, int materialCount, Ingredient catalyst, ItemStack result, int time)
        implements Recipe<PressInput> {

    public static final MapCodec<PressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("material").forGetter(PressRecipe::material),
            Codec.intRange(1, 64).optionalFieldOf("material_count", 1).forGetter(PressRecipe::materialCount),
            Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(PressRecipe::catalyst),
            ItemStack.CODEC.fieldOf("result").forGetter(PressRecipe::result),
            Codec.intRange(1, 1_000_000).fieldOf("time").forGetter(PressRecipe::time))
            .apply(instance, PressRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PressRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, PressRecipe::material,
            ByteBufCodecs.VAR_INT, PressRecipe::materialCount,
            Ingredient.CONTENTS_STREAM_CODEC, PressRecipe::catalyst,
            ItemStack.STREAM_CODEC, PressRecipe::result,
            ByteBufCodecs.VAR_INT, PressRecipe::time,
            PressRecipe::new);

    /** Serializer of {@link PressRecipe}. */
    public static final class Serializer implements RecipeSerializer<PressRecipe> {
        @Override
        public MapCodec<PressRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PressRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    @Override
    public boolean matches(PressInput input, Level level) {
        return material.test(input.material()) && input.material().getCount() >= materialCount
                && catalyst.test(input.catalyst());
    }

    @Override
    public ItemStack assemble(PressInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, material, catalyst);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModPress.PRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModPress.PRESS_TYPE.get();
    }
}
