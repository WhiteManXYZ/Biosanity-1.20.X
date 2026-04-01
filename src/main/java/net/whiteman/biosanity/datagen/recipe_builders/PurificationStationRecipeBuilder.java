package net.whiteman.biosanity.datagen.recipe_builders;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static net.whiteman.biosanity.world.util.ModifierUtils.ModifierType;

public class PurificationStationRecipeBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final ModifierType modifier;
    private final DyeColor color;
    private final int count;
    private final int processingTime;
    private final RecipeSerializer<?> serializer;

    public PurificationStationRecipeBuilder(Item result, Ingredient ingredient, ModifierType modifier, int count, int processingTime, DyeColor color, RecipeSerializer<?> serializer) {
        this.result = result;
        this.ingredient = ingredient;
        this.modifier = modifier;
        this.color = color;
        this.count = count;
        this.processingTime = processingTime;
        this.serializer = serializer;
    }

    /**
     * Purification station recipe generator.
     * @param result Output item.
     * @param ingredient Input item.
     * @param modifier Modifier type (sand, dye e.t.c)
     * @param count Output item count.
     * @param time Purification or painting time e.g.
     * @param color Modifier color. If recipe need no color, set always to DyeColor.WHITE
     * @param serializer Type of recipe.
     */
    public static PurificationStationRecipeBuilder recipe(Item result, Ingredient ingredient, ModifierType modifier, int count, int time, DyeColor color, RecipeSerializer<?> serializer) {
        return new PurificationStationRecipeBuilder(result, ingredient, modifier, count, time, color, serializer);
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.result, this.ingredient, this.modifier, this.count, this.processingTime, this.color, this.serializer));
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final Ingredient ingredient;
        private final ModifierType modifier;
        private final DyeColor color;
        private final int count;
        private final int processingTime;
        private final RecipeSerializer<?> serializer;

        public Result(ResourceLocation id, Item result, Ingredient ingredient, ModifierType modifier, int count, int processingTime, DyeColor color, RecipeSerializer<?> serializer) {
            this.id = id;
            this.result = result;
            this.ingredient = ingredient;
            this.modifier = modifier;
            this.color = color;
            this.count = count;
            this.processingTime = processingTime;
            this.serializer = serializer;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", this.ingredient.toJson());
            json.addProperty("modifier", this.modifier.name().toLowerCase());
            json.addProperty("color", this.color.name().toLowerCase());

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result), "Item not registered: " + this.result).toString());

            resultJson.addProperty("count", this.count);
            json.add("result", resultJson);
            json.addProperty("processingTime", this.processingTime);
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return this.serializer;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() { return null; }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() { return null; }
    }
}