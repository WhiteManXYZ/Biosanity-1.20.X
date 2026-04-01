package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.world.neoplasm.core.CoreConfig.MAX_ALERT_POINTS;

public class NeoplasmCoreBlockEntity extends BlockEntity {
    public static final int CALM_DOWN_RATE = 5;

    public NeoplasmCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_CORE_BE.get(), pPos, pBlockState);
    }

    // TODO make different variants for different systems
    private int currentXp = CoreLevel.getStartingXp();
    private int alertPoints = 0;
    private AlertLevel alertLevel;

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
        this.alertLevel = AlertLevel.fromPoints(this.alertPoints);
    }

    public void decreaseAlertPoints(int amount) {
        if (this.alertPoints <= 0) return;

        this.alertPoints = Math.max(alertPoints - amount, 0);
        this.alertLevel = AlertLevel.fromPoints(this.alertPoints);
    }

    public void decomposeResource(ResourceType type, int level) {
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
        return CoreConfig.getXPFromLevel(level);
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
