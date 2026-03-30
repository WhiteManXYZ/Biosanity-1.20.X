package net.whiteman.biosanity.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.util.block.NeoplasmRegistry.CoreLevel;

public class NeoplasmCoreBlockEntity extends BlockEntity {
    public NeoplasmCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_CORE_BE.get(), pPos, pBlockState);
    }

    private int currentXp = CoreLevel.getStartingXp();
    private int alertPoints = 0;
    private int alertLevel; // TODO enum type

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmCoreBlockEntity blockEntity) {
        if (level.hasNearbyAlivePlayer(pos.getX(), pos.getY(), pos.getZ(), 10D)) alertPoints++;
        else alertPoints--;
        System.out.println("[NP: CORE] ticking. " + currentXp + " current experience, " + "alert points: " + alertPoints);
    }

    protected void spreadInfection(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {}

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
    }
}
