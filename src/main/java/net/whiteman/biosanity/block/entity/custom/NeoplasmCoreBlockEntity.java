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

import static net.whiteman.biosanity.util.block.NeoplasmRegistry.*;

public class NeoplasmCoreBlockEntity extends BlockEntity {
    public static final int CALM_DOWN_RATE = 5;
    public static final int RESOURCE_LEVEL_MULTIPLIER = 10;

    private static final int[] XP_TABLE = {3, 10, 30, 90, 270, 810, 2430};

    public NeoplasmCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_CORE_BE.get(), pPos, pBlockState);
    }

    // TODO make different variants for different systems
    private int currentXp = CoreLevel.getStartingXp();
    private int alertPoints = 0;
    private CoreAlertLevel alertLevel;

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmCoreBlockEntity blockEntity) {


        // Every 1-second block
        if (level.getGameTime() % 20 == 0) {
            // Calm down over time
            blockEntity.decreaseAlertPoints(CALM_DOWN_RATE);
        }
    }

    private void spreadInfection(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {}

    public void increaseAlertPoints(int amount) {
        if (this.alertPoints >= MAX_ALERT_POINTS) return;

        this.alertPoints = Math.min(alertPoints + amount, MAX_ALERT_POINTS);
        this.alertLevel = CoreAlertLevel.fromPoints(this.alertPoints);
    }

    public void decreaseAlertPoints(int amount) {
        if (this.alertPoints <= 0) return;

        this.alertPoints = Math.max(alertPoints - amount, 0);
        this.alertLevel = CoreAlertLevel.fromPoints(this.alertPoints);
    }

    public void decomposeResource(int level, ResourceType type) {
        switch (type) {
            case BIOMASS -> {
                // Some code
                System.out.println("gained biomass");
                System.out.println(level);

            }
            case MINERAL -> {
                // Some code
                System.out.println("gained mineral");
                System.out.println(level);

            }
            case ENERGY -> {
                // Some code
                System.out.println("gained energy");
                System.out.println(level);
            }
        }
        System.out.println("With level " + level + " result xp: " + calculateXp(level));
    }

    private int calculateXp(int level) {
        return (int) (RESOURCE_LEVEL_MULTIPLIER * Math.pow(3, level - 1));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
    }
}
