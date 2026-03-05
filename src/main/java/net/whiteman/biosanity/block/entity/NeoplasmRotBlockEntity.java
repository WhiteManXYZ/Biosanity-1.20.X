package net.whiteman.biosanity.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.whiteman.biosanity.client.model.ModelProperties;
import org.jetbrains.annotations.NotNull;

public class NeoplasmRotBlockEntity extends BlockEntity {
    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private int overlayStage = 0;
    private double dropChance = 0.7;

    public NeoplasmRotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEOPLASM_ROT_BE.get(), pos, state);
    }


    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ModelProperties.ORIGINAL_STATE, originalState)
                .with(ModelProperties.OVERLAY_STAGE, overlayStage)
                .build();
    }

    public void setInfectionData(BlockState originalState, int stage) {
        this.originalState = originalState;
        this.overlayStage = stage;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }


    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldState = this.originalState;
        int oldStage = this.overlayStage;

        super.onDataPacket(net, pkt);

        if (this.level != null && (this.originalState != oldState || this.overlayStage != oldStage)) {
            requestModelDataUpdate();
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        setChanged();
    }

    public BlockState getOriginalState() {
        return this.originalState;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("DropChance", this.dropChance);
        tag.put("OriginalBlock", NbtUtils.writeBlockState(originalState));
        tag.putInt("OverlayStage", overlayStage);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.dropChance = tag.getDouble("DropChance");
        if (tag.contains("OriginalBlock", 10)) { // 10 - compound tag
            HolderGetter<Block> holdergetter = this.level != null ?
                    this.level.holderLookup(Registries.BLOCK) :
                    BuiltInRegistries.BLOCK.asLookup();

            this.originalState = NbtUtils.readBlockState(holdergetter, tag.getCompound("OriginalBlock"));
        }
        this.overlayStage = tag.getInt("OverlayStage");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void decreaseChance(double rate) {
        this.dropChance = Math.max(this.dropChance - rate, 0);
        this.setChanged();
    }

    public double getDropChance() {
        return this.dropChance;
    }
}