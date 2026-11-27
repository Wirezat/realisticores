package de.wirezat.realisticores.compat.jei;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.indicator.Indicators;
import de.wirezat.realisticores.ore.Deposit;
import de.wirezat.realisticores.ore.Indicator;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** JEI category explaining, per deposit, which grains the pan finds on its trail and how far each travels. */
public class ProspectingCategory implements IRecipeCategory<ProspectingCategory.Recipe> {
    public static final RecipeType<Recipe> TYPE = RecipeType.create(RealisticOres.MOD_ID, "prospecting", Recipe.class);
    private static final int WIDTH = 160;
    private static final int HEIGHT = 88;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int SLOT_ROW = 12;
    private static final int FIRST_GRAIN_X = 70;
    private static final int GRAIN_STEP = 24;
    private static final int MAP_X = 142;
    private static final int DESCRIPTION_Y = 44;

    private final IDrawable icon;

    /**
     * A trail to follow: one deposit and the map that leads onto its trail.
     *
     * @param deposit the deposit with at least one indicator mineral
     * @param map the survey map of the deposit, empty if it has none
     */
    public record Recipe(Deposit deposit, List<Item> map) {
        /**
         * Lists the indicators from the one that travels farthest to the one found only near the deposit.
         *
         * @return the indicators of the deposit sorted by falling trail length
         */
        List<Indicator> farthestFirst() {
            return deposit.indicators().stream().sorted(Comparator.comparingDouble(Indicator::length).reversed())
                    .toList();
        }
    }

    /**
     * Creates the category.
     *
     * @param helper JEI drawing helper
     */
    public ProspectingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(ModItems.IRON_PAN.get());
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.realisticores.prospecting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, SLOT_ROW).setStandardSlotBackground()
                .addItemStacks(List.of(new ItemStack(Items.SAND), new ItemStack(Items.GRAVEL),
                        new ItemStack(Items.DIRT)));
        builder.addSlot(RecipeIngredientRole.CATALYST, 21, SLOT_ROW).setStandardSlotBackground()
                .addItemLike(ModItems.IRON_PAN.get());
        List<Indicator> grains = recipe.farthestFirst();
        for (int i = 0; i < grains.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, FIRST_GRAIN_X + i * GRAIN_STEP, SLOT_ROW)
                    .setStandardSlotBackground().addItemLike(Indicators.item(grains.get(i)));
        }
        recipe.map().forEach(map -> builder.addSlot(RecipeIngredientRole.CATALYST, MAP_X, SLOT_ROW)
                .setStandardSlotBackground().addItemLike(map));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Recipe recipe, IFocusGroup focuses) {
        String id = recipe.deposit().id();
        builder.addText(Component.translatable("jei.realisticores.prospecting." + id + ".name"), WIDTH, 10)
                .setColor(TEXT_COLOR);
        builder.addRecipeArrowWidget().setPosition(43, SLOT_ROW + 1);
        List<Indicator> grains = recipe.farthestFirst();
        for (int i = 0; i < grains.size(); i++) {
            Component distance = Component.translatable("jei.realisticores.prospecting.distance",
                    Math.round(grains.get(i).length()));
            builder.addText(distance, GRAIN_STEP, 10).setPosition(FIRST_GRAIN_X - 3 + i * GRAIN_STEP, SLOT_ROW + 20)
                    .setTextAlignment(HorizontalAlignment.CENTER).setColor(TEXT_COLOR);
        }
        builder.addText(Component.translatable("jei.realisticores.prospecting." + id), WIDTH,
                HEIGHT - DESCRIPTION_Y).setPosition(0, DESCRIPTION_Y).setColor(TEXT_COLOR);
    }
}
