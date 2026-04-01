package net.whiteman.biosanity.client.resources.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.List;

public class OverlayModelLoader implements IGeometryLoader<OverlayUnbakedGeometry> {
    public static final ResourceLocation ID = new ResourceLocation("biosanity", "util_overlay");

    @Override
    public OverlayUnbakedGeometry read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        List<ResourceLocation> overlayTextures = new ArrayList<>();
        if (jsonObject.has("overlays")) {
            JsonArray overlaysArray = jsonObject.getAsJsonArray("overlays");
            overlaysArray.forEach(element -> overlayTextures.add(new ResourceLocation(element.getAsString())));
        }

        ResourceLocation fallbackModel = new ResourceLocation(GsonHelper.getAsString(jsonObject, "fallback_model", "minecraft:block/stone"));

        return new OverlayUnbakedGeometry(overlayTextures, fallbackModel);
    }
}