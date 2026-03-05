package net.whiteman.biosanity.client.model;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelProperties {
    public static final ModelProperty<BlockState> ORIGINAL_STATE = new ModelProperty<>();
    public static final ModelProperty<Integer> OVERLAY_STAGE = new ModelProperty<>();
}