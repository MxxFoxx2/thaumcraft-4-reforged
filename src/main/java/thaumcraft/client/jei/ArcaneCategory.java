package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.registry.ModBlocks;

final class ArcaneCategory implements IRecipeCategory<ArcaneRecipe> {
    static final RecipeType<ArcaneRecipe> TYPE = new RecipeType<>(Thaumcraft.id("arcane"), ArcaneRecipe.class);
    private static final int WIDTH = 130;
    private static final int PER_ROW = 7;
    private static final int COST_Y = 60;

    private final IGuiHelper helper;
    private final IDrawable icon;

    ArcaneCategory(IGuiHelper helper) {
        this.helper = helper;
        this.icon = helper.createDrawableItemLike(ModBlocks.ARCANE_WORKTABLE.get());
    }

    @Override
    public RecipeType<ArcaneRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.type.arcane");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return COST_Y + 20;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ArcaneRecipe recipe, IFocusGroup focuses) {
        if (recipe instanceof ShapedArcaneRecipe shaped) {
            for (int column = 0; column < shaped.width() && column < 3; column++) {
                for (int row = 0; row < shaped.height() && row < 3; row++) {
                    int index = column + row * shaped.width();
                    if (index < shaped.grid().size()) {
                        TcJei.accept(builder.addInputSlot(column * 18, row * 18).setStandardSlotBackground(), shaped.grid().get(index));
                    }
                }
            }
        } else if (recipe instanceof ShapelessArcaneRecipe shapeless) {
            List<TcIngredient> items = shapeless.ingredients();
            for (int index = 0; index < items.size() && index < 9; index++) {
                TcJei.accept(builder.addInputSlot(index % 3 * 18, index / 3 * 18).setStandardSlotBackground(), items.get(index));
            }
            builder.setShapeless();
        }
        TcJei.accept(builder.addOutputSlot(104, 18).setOutputSlotBackground(), recipe.result());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemLike(ModBlocks.ARCANE_WORKTABLE.get());
    }

    @Override
    public void draw(ArcaneRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        helper.getRecipeArrow().draw(graphics, 70, 19);
        AspectStrip.draw(graphics, recipe.aspects(), PER_ROW, WIDTH, COST_Y, 1);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ArcaneRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        AspectStrip.tooltip(tooltip, recipe.aspects(), PER_ROW, WIDTH, COST_Y, mouseX, mouseY);
    }
}
