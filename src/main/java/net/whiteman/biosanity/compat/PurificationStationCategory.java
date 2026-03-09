package net.whiteman.biosanity.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.recipe.PurificationStationRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PurificationStationCategory implements IRecipeCategory<PurificationStationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(BiosanityMod.MOD_ID, "purification");
    public static final ResourceLocation TEXTURE = new ResourceLocation(BiosanityMod.MOD_ID,
            "textures/gui/purification_station_block_gui.png");

    public static final RecipeType<PurificationStationRecipe> PURIFICATION_TYPE =
            new RecipeType<>(UID, PurificationStationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated animatedArrow;
    private final IDrawableAnimated bubbles;

    public PurificationStationCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 48, 16, 121, 53);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PURIFICATION_STATION_BLOCK.get()));

        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE, 176, 0, 24, 17);
        this.animatedArrow = helper.createAnimatedDrawable(staticArrow, 400, IDrawableAnimated.StartDirection.LEFT, false);

        ITickTimer bubblesTickTimer = new PurificationBubblesTickTimer(helper);
        bubbles = helper.drawableBuilder(TEXTURE, 176, 29, 11, 11)
                .buildAnimated(bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);
    }

    @Override
    public RecipeType<PurificationStationRecipe> getRecipeType() {
        return PURIFICATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.block.action.biosanity.purification_translatable");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PurificationStationRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 24).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 24).addItemStack(recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public void draw(PurificationStationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        this.animatedArrow.draw(guiGraphics, 44, 24);
        bubbles.draw(guiGraphics, 6, 35);
    }

    private static class PurificationBubblesTickTimer implements ITickTimer {
        private static final int[] BUBBLE_LENGTHS = new int[]{10, 5, 0};
        private final ITickTimer internalTimer;

        public PurificationBubblesTickTimer(IGuiHelper guiHelper) {
            this.internalTimer = guiHelper.createTickTimer(12, BUBBLE_LENGTHS.length - 1, false);
        }

        @Override
        public int getValue() {
            int timerValue = this.internalTimer.getValue();
            return BUBBLE_LENGTHS[timerValue];
        }

        @Override
        public int getMaxValue() {
            return BUBBLE_LENGTHS[0];
        }
    }
}

