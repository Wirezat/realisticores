package de.wirezat.realisticores.compat.jei;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.press.PressRecipe;
import de.wirezat.realisticores.press.PressSeed;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

/** JEI category showing the crystal press recipes. */
public class PressCategory implements IRecipeCategory<PressRecipe> {
    public static final RecipeType<PressRecipe> TYPE =
            RecipeType.create(RealisticOres.MOD_ID, "crystal_press", PressRecipe.class);
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "textures/gui/crystal_press.png");
    private static final int ORIGIN_X = 12;
    private static final int ORIGIN_Y = 11;
    private static final int WIDTH = 156;
    private static final int HEIGHT = 63;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable flame;
    private final IDrawable arrow;
    private final IDrawable upperAnvil;
    private final IDrawable lowerAnvil;
    private final IDrawable crystal;

    /**
     * Creates the category.
     *
     * @param helper JEI drawing helper
     */
    public PressCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, ORIGIN_X, ORIGIN_Y, WIDTH, HEIGHT);
        this.icon = helper.createDrawableItemLike(ModBlocks.CRYSTAL_PRESS.get());
        IDrawableStatic flameSprite = helper.createDrawable(TEXTURE, 210, 34, 14, 14);
        this.flame = helper.createAnimatedDrawable(flameSprite, 300, IDrawableAnimated.StartDirection.TOP, true);
        IDrawableStatic arrowSprite = helper.createDrawable(TEXTURE, 176, 56, 22, 15);
        this.arrow = helper.createAnimatedDrawable(arrowSprite, 200, IDrawableAnimated.StartDirection.LEFT, false);
        this.upperAnvil = helper.createDrawable(TEXTURE, 176, 0, 28, 17);
        this.lowerAnvil = helper.createDrawable(TEXTURE, 176, 17, 28, 17);
        this.crystal = helper.createDrawable(TEXTURE, 84, 168, 14, 14);
    }

    @Override
    public RecipeType<PressRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.realisticores.crystal_press");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> materials = Arrays.stream(recipe.material().getItems())
                .map(stack -> stack.copyWithCount(recipe.materialCount())).toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 43 - ORIGIN_X, 18 - ORIGIN_Y).addItemStacks(materials);
        builder.addSlot(RecipeIngredientRole.INPUT, 17 - ORIGIN_X, 18 - ORIGIN_Y)
                .addItemStacks(Arrays.asList(recipe.catalyst().getItems()));
        builder.addSlot(RecipeIngredientRole.CATALYST, 43 - ORIGIN_X, 52 - ORIGIN_Y)
                .addItemStack(new ItemStack(Items.COAL));
        builder.addSlot(RecipeIngredientRole.CATALYST, 17 - ORIGIN_X, 52 - ORIGIN_Y).addItemStacks(PressSeed.all());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 144 - ORIGIN_X, 35 - ORIGIN_Y).addItemStack(recipe.result());
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics);
        upperAnvil.draw(graphics, 78 - ORIGIN_X, 18 - ORIGIN_Y);
        lowerAnvil.draw(graphics, 78 - ORIGIN_X, 51 - ORIGIN_Y);
        crystal.draw(graphics, 85 - ORIGIN_X, 36 - ORIGIN_Y);
        flame.draw(graphics, 44 - ORIGIN_X, 36 - ORIGIN_Y);
        arrow.draw(graphics, 114 - ORIGIN_X, 36 - ORIGIN_Y);
        Component time = Component.translatable("gui.realisticores.crystal_press.time", recipe.time() / 20);
        graphics.drawString(Minecraft.getInstance().font, time, 116 - ORIGIN_X, 60 - ORIGIN_Y, 0xFF404040, false);
    }
}
