package net.whiteman.biosanity.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OverlayBakedModel implements BakedModel {
    private final BakedModel fallbackModel;
    private final List<TextureAtlasSprite> overlaySprites;

    public OverlayBakedModel(BakedModel fallbackModel, List<TextureAtlasSprite> overlaySprites) {
        this.fallbackModel = fallbackModel;
        this.overlaySprites = overlaySprites;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState originalState = data.get(ModelProperties.ORIGINAL_STATE);
        Integer stage = data.get(ModelProperties.OVERLAY_STAGE);

        // Fallback, if the block is not infected or the data is corrupted
        if (originalState == null || originalState.isAir()) {
            return fallbackModel.getQuads(state, side, rand, data, renderType);
        }

        BlockModelShaper blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        BakedModel originalModel = blockModelShaper.getBlockModel(originalState);

        List<BakedQuad> resultQuads = new ArrayList<>();

        // Adding basic quads (original block)
        if (renderType == null || originalModel.getRenderTypes(originalState, rand, data).contains(renderType)) {
            resultQuads.addAll(originalModel.getQuads(originalState, side, rand, data, renderType));
        }

        // Adding overlay quads (details)
        if (stage != null && stage >= 0 && stage < overlaySprites.size() &&
                (renderType == null || renderType == RenderType.translucent())) {

            TextureAtlasSprite overlaySprite = overlaySprites.get(stage);
            List<BakedQuad> baseQuads = originalModel.getQuads(originalState, side, rand, data, null);

            for (BakedQuad quad : baseQuads) {
                resultQuads.add(createOverlayQuad(quad, overlaySprite));
            }
        }

        return resultQuads;
    }

    private BakedQuad createOverlayQuad(BakedQuad baseQuad, TextureAtlasSprite newSprite) {
        int[] vertexData = baseQuad.getVertices().clone();
        int step = vertexData.length / 4;
        TextureAtlasSprite baseSprite = baseQuad.getSprite();

        for (int i = 0; i < 4; i++) {
            int offset = i * step;

            // Position
            float x = Float.intBitsToFloat(vertexData[offset]);
            float y = Float.intBitsToFloat(vertexData[offset + 1]);
            float z = Float.intBitsToFloat(vertexData[offset + 2]);

            // Z-fighting protection
            Direction dir = baseQuad.getDirection();
            vertexData[offset] = Float.floatToRawIntBits(x + dir.getStepX() * 0.0005f);
            vertexData[offset + 1] = Float.floatToRawIntBits(y + dir.getStepY() * 0.0005f);
            vertexData[offset + 2] = Float.floatToRawIntBits(z + dir.getStepZ() * 0.0005f);

            // UV-coords
            float u = Float.intBitsToFloat(vertexData[offset + 4]);
            float v = Float.intBitsToFloat(vertexData[offset + 5]);

            // Direct calculation of relative UV with clamp of 0..1
            float relU = (u - baseSprite.getU0()) / (baseSprite.getU1() - baseSprite.getU0());
            float relV = (v - baseSprite.getV0()) / (baseSprite.getV1() - baseSprite.getV0());

            // Scale it to 0..16 and take the UV overlay
            vertexData[offset + 4] = Float.floatToRawIntBits(newSprite.getU(Math.max(0, Math.min(1, relU)) * 16.0f));
            vertexData[offset + 5] = Float.floatToRawIntBits(newSprite.getV(Math.max(0, Math.min(1, relV)) * 16.0f));
        }

        return new BakedQuad(vertexData, baseQuad.getTintIndex(), baseQuad.getDirection(), newSprite, baseQuad.isShade());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    // BakedModel require this methods
    @Override
    public boolean useAmbientOcclusion() { return true; }
    @Override
    public boolean isGui3d() { return true; }
    @Override
    public boolean usesBlockLight() { return true; }
    @Override
    public boolean isCustomRenderer() { return false; }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return fallbackModel.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState originalState = data.get(ModelProperties.ORIGINAL_STATE);
        if (originalState != null && !originalState.isAir()) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(originalState).getParticleIcon(data);
        }
        return fallbackModel.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.solid(), RenderType.translucent());
    }
}