package net.whiteman.biosanity.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.world.util.ModifierUtils.ModifierType;

public class PurificationRecipe extends AbstractJettingRecipe {
    public PurificationRecipe(ResourceLocation id, ItemStack output, Ingredient input, ModifierType modifier, int time, DyeColor color) {
        super(id, output, input, modifier, time, color);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.PURIFICATION_SERIALIZER.get(); }
    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.PURIFICATION_TYPE.get(); }
}
