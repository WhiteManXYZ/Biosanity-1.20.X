package net.whiteman.biosanity.client.resources.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.List;
import java.util.function.Function;

public class OverlayUnbakedGeometry implements IUnbakedGeometry<OverlayUnbakedGeometry> {
    private final List<ResourceLocation> overlayTextures;
    private final ResourceLocation fallbackModelLocation;

    public OverlayUnbakedGeometry(List<ResourceLocation> overlayTextures, ResourceLocation fallbackModelLocation) {
        this.overlayTextures = overlayTextures;
        this.fallbackModelLocation = fallbackModelLocation;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        modelGetter.apply(fallbackModelLocation).resolveParents(modelGetter);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel fallbackModel = baker.bake(fallbackModelLocation, modelState, spriteGetter);

        List<TextureAtlasSprite> bakedOverlays = overlayTextures.stream()
                .map(loc -> spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, loc)))
                .toList();

        return new OverlayBakedModel(fallbackModel, bakedOverlays);
    }
}
