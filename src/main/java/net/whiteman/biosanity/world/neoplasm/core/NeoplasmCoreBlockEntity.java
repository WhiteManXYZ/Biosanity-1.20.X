package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.NeoplasmConfig;
import net.whiteman.biosanity.world.neoplasm.ai.IHivemindGoal;
import net.whiteman.biosanity.world.neoplasm.common.node.INeoplasmNode;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindManager;
import net.whiteman.biosanity.world.neoplasm.resource.ResourceType;
import net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.whiteman.biosanity.world.neoplasm.NeoplasmConfig.*;
import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.FACING;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.PARENT_DIRECTION;

public class NeoplasmCoreBlockEntity extends BlockEntity {
    private UUID hivemindId;
    private IHivemindGoal currentGoal;

    /** Seeded parameter that offsets a little goal cooldown,
     * to prevent actions in same tick */
    private int goalTickOffset;
    /** Seeded parameter that offsets a little goal condition,
     * to randomize each cores */
    private int goalConditionOffset;

    public NeoplasmCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_CORE_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmCoreBlockEntity blockEntity) {
        if (level.isClientSide) return;

        Hivemind hive = HivemindManager.get(level).getHivemindByPos(pos);
        if (hive == null) {
            return;
        }

        if (this.currentGoal != null) {
            if (this.currentGoal.canContinueToUse()) {
                this.currentGoal.tick();
            } else {
                this.setCurrentGoal(null);
            }
        }
    }

    //region Core actions

    /** Decomposes resource for his {@link Hivemind}
     * @param type {@link ResourceType}
     * @param level The base nutrient value of the decomposed material (e.g. oak log has level 1)
     * @return Result of decomposing, was it successful or not
     */
    public boolean decomposeResource(ResourceType type, int level) {
        Hivemind hive = getHivemind();
        if (hive == null || level <= 0 || level > MAX_RESOURCE_LEVEL) return false;

        hive.modifyExperiencePoints(NeoplasmConfig.getXPFromLevel(level));

        switch (type) {
            case BIOMASS -> hive.modifyBiomass(NeoplasmConfig.getNutrientsFromLevel(level));
            case MINERAL -> hive.modifyMinerals(NeoplasmConfig.getNutrientsFromLevel(level));
            case ENERGY -> hive.modifyEnergy(NeoplasmConfig.getNutrientsFromLevel(level));
            default -> throw new IllegalArgumentException("Unknown resource type: " + type);
        }

        return true;
    }

    public boolean growNewVein(Direction dir) {
        if (this.level == null || level.isClientSide || dir == null) return false;
        boolean flag;

        BlockPos targetPos = this.worldPosition.relative(dir);
        flag = level.setBlock(targetPos, ModBlocks.NEOPLASM_VEIN_BLOCK.get().defaultBlockState()
                    .setValue(FACING, dir)
                    .setValue(PARENT_DIRECTION, dir.getOpposite()), 3);

        if (flag) {
            // TEST PARTICLE
            ((ServerLevel)level).sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.8, this.worldPosition.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.15);
        }

        return flag;
    }

    public boolean expandCore(Direction dir) {
        if (this.level == null || level.isClientSide) return false;
        boolean flag;

        BlockPos targetPos = this.worldPosition.relative(dir);
        flag = level.setBlock(targetPos, ModBlocks.NEOPLASM_CORE_BLOCK.get().defaultBlockState(), 3);

        if (flag) {
            // TEST PARTICLE
            DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);

            ((ServerLevel)level).sendParticles(redDust,
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5,
                    5, 0.3, 0.3, 0.3, 0.05);
        }

        return flag;
    }
    //endregion

    //region Hivemind
    public void setCurrentGoal(IHivemindGoal newGoal) {
        if (this.currentGoal == newGoal) return;

        if (this.currentGoal != null) {
            this.currentGoal.stop();
        }

        this.currentGoal = newGoal;

        if (this.currentGoal != null) {
            this.currentGoal.start();
        }
    }

    public IHivemindGoal getCurrentGoal() { return currentGoal; }

    public UUID getHivemindId() { return this.hivemindId; }

    public @Nullable Hivemind getHivemind() {
        if (this.level == null || this.hivemindId == null) return null;

        HivemindManager data = HivemindManager.get(this.level);
        return data != null ? data.getHivemindById(this.hivemindId) : null;
    }
    
    public void setHivemindId(UUID id) {
        this.hivemindId = id;
        this.setChanged();
    }

    public @Nullable List<BlockPos> findNeighborCores() {
        if (this.level == null || level.isClientSide) return null;
        List<BlockPos> cores = new ArrayList<>();
        for (Direction dir : DIRECTIONS) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() instanceof INeoplasmNode node && node.isCore()) {
                cores.add(neighborPos);
            }
        }
        return cores;
    }

    public @Nullable List<BlockPos> findNeighborVeins() {
        if (this.level == null || level.isClientSide) return null;
        List<BlockPos> veins = new ArrayList<>();
        for (Direction dir : DIRECTIONS) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() instanceof NeoplasmVeinBlock) {
                veins.add(neighborPos);
            }
        }
        return veins;
    }
    //endregion

    //region Characteristics
    public int getGoalTickOffset() {
        return goalTickOffset;
    }

    public int getGoalConditionOffset() {
        return goalConditionOffset;
    }
    //endregion

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && this.hivemindId != null) {
            Hivemind hive = getHivemind();
            if (hive != null) {
                hive.addMember(this.worldPosition);
                HivemindManager.get(level).registerBlock(this.worldPosition, this.hivemindId);
            }
        }

        // Each core takes on a small tick offset, that based on position
        Random random = new Random(this.worldPosition.asLong());
        this.goalTickOffset = random.nextInt(CORE_GOAL_TICK_THRESHOLD + 1) - CORE_GOAL_TICK_OFFSET;
        // Take on a small condition offset, that based on position too (but has another value)
        this.goalConditionOffset = random.nextInt(CORE_GOAL_CONDITION_THRESHOLD + 1) - CORE_GOAL_CONDITION_OFFSET;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        if (this.hivemindId != null) {
            pTag.putUUID("hivemindId", hivemindId);
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.hasUUID("hivemindId")) {
            this.hivemindId = pTag.getUUID("hivemindId");
        }
    }
}
