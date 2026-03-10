package net.whiteman.biosanity.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.recipe.PurificationStationRecipe;
import net.whiteman.biosanity.screen.PurificationStationBlockScreen;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIBiosanityPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(BiosanityMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PurificationStationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<PurificationStationRecipe> purificationRecipes = recipeManager.getAllRecipesFor(PurificationStationRecipe.Type.INSTANCE);
        registration.addRecipes(PurificationStationCategory.PURIFICATION_TYPE, purificationRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.PURIFICATION_STATION_BLOCK.get()),
                PurificationStationCategory.PURIFICATION_TYPE
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PurificationStationBlockScreen.class, 92, 37, 25, 21,
                PurificationStationCategory.PURIFICATION_TYPE);
    }
}
