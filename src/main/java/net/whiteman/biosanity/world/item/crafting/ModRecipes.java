package net.whiteman.biosanity.world.item.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZER =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BiosanityMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, BiosanityMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<PurificationRecipe>> PURIFICATION_SERIALIZER =
            SERIALIZER.register("purification", () -> new SimpleJettingSerializer<>(PurificationRecipe::new));

    public static final RegistryObject<RecipeSerializer<PaintingRecipe>> PAINTING_SERIALIZER =
            SERIALIZER.register("painting", () -> new SimpleJettingSerializer<>(PaintingRecipe::new));

    public static final RegistryObject<RecipeType<PurificationRecipe>> PURIFICATION_TYPE =
            TYPES.register("purification", () -> new RecipeType<PurificationRecipe>() {
                @Override public String toString() { return "purification"; }
            });

    public static final RegistryObject<RecipeType<PaintingRecipe>> PAINTING_TYPE =
            TYPES.register("painting", () -> new RecipeType<PaintingRecipe>() {
                @Override public String toString() { return "painting"; }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZER.register(eventBus);
        TYPES.register(eventBus);
    }
}
