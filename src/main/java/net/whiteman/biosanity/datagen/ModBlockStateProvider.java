package net.whiteman.biosanity.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.block.RedstoneUVLampBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BiosanityMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        customLamp();

        blockWithItem(ModBlocks.NEOPLASM_BLOCK);

        blockWithItem(ModBlocks.NEOPLASM_CORE_BLOCK);

        blockWithItem(ModBlocks.NEOPLASM_VEIN_BLOCK);

        blockWithItem(ModBlocks.NETHER_ALGANIT_ORE);

        blockWithOverlay(ModBlocks.NEOPLASM_ROT_BLOCK,
                "neoplasm_rot_block", // Fallback model
                "overlays/rot_stages/neoplasm_rot_stage_0",
                "overlays/rot_stages/neoplasm_rot_stage_1",
                "overlays/rot_stages/neoplasm_rot_stage_2"
        );
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void customLamp() {
        getVariantBuilder(ModBlocks.UV_LAMP_BLOCK.get()).forAllStates(state -> {
            if (state.getValue(RedstoneUVLampBlock.LIT)) {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block_lit",
                        new ResourceLocation(BiosanityMod.MOD_ID, "block/" + "uv_lamp_block_lit")))};
            } else {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block",
                        new ResourceLocation(BiosanityMod.MOD_ID, "block/" + "uv_lamp_block")))};
            }
        });

        simpleBlockItem(ModBlocks.UV_LAMP_BLOCK.get(), models().cubeAll("uv_lamp_block",
                new ResourceLocation(BiosanityMod.MOD_ID, "block/" + "uv_lamp_block")));
    }

    private void blockWithOverlay(RegistryObject<Block> block, String baseTextureName, String... overlayTextureNames) {
        String name = block.getId().getPath();

        BlockModelBuilder baseModel = models().cubeAll(name + "_base",
                new ResourceLocation(BiosanityMod.MOD_ID, "block/" + baseTextureName));

        BlockModelBuilder builder = models().getBuilder(name)
                .customLoader((b, h) -> new CustomLoaderBuilder<BlockModelBuilder>(
                        new ResourceLocation(BiosanityMod.MOD_ID, "util_overlay"), b, h) {
                    @Override
                    public JsonObject toJson(JsonObject json) {
                        JsonObject root = new JsonObject();
                        root.addProperty("loader", new ResourceLocation(BiosanityMod.MOD_ID, "util_overlay").toString());

                        root.addProperty("fallback_model", baseModel.getLocation().toString());

                        JsonArray arr = new JsonArray();
                        for (String tex : overlayTextureNames) {
                            arr.add(new ResourceLocation(BiosanityMod.MOD_ID, "block/" + tex).toString());
                        }
                        root.add("overlays", arr);
                        return root;
                    }
                }).end();

        simpleBlock(block.get(), builder);
        itemModels().getBuilder(name).parent(baseModel);
    }
}
