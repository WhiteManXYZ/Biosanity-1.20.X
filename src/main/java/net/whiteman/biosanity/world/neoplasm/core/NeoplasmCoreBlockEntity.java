package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.common.node.INeoplasmNode;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindManager;
import net.whiteman.biosanity.world.neoplasm.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.FACING;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.PARENT_DIRECTION;

public class NeoplasmCoreBlockEntity extends BlockEntity {
    private UUID hivemindId;

    public NeoplasmCoreBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_CORE_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmCoreBlockEntity blockEntity) {
        if (level.isClientSide) return;

        Hivemind hive = HivemindManager.get(level).getHivemindByPos(pos);

        if (hive == null) {
            return;
        }

        // Some code
    }
    
    public void decomposeResource(ResourceType type, int level) {
        Hivemind hive = getHivemind();
        if (hive == null || level <= 0 || level > MAX_RESOURCE_LEVEL) return;

        hive.modifyExperiencePoints(CoreConfig.getXPFromLevel(level));

        switch (type) {
            case BIOMASS -> hive.modifyBiomass(CoreConfig.getNutrientsFromLevel(level));
            case MINERAL -> hive.modifyMinerals(CoreConfig.getNutrientsFromLevel(level));
            case ENERGY -> hive.modifyEnergy(CoreConfig.getNutrientsFromLevel(level));
        }
    }

    private void spreadInfection(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {}

    public void growNewVein(Level level, BlockPos pos) {
        /// WIP
        Direction dir;
        do {
            dir = Direction.getRandom(level.random);
        } while (dir.getAxis().isVertical());

        BlockPos targetPos = pos.relative(dir);
        if (level.getBlockState(targetPos).isAir()) {
            level.setBlock(targetPos, ModBlocks.NEOPLASM_VEIN_BLOCK.get().defaultBlockState()
                    .setValue(FACING, dir)
                    .setValue(PARENT_DIRECTION, dir.getOpposite()), 3);
        }
    }

    //region Hivemind
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
    //endregion

    public List<BlockPos> findNeighborCores(Level level) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (Direction dir : DIRECTIONS) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() instanceof INeoplasmNode node && node.isCore()) {
                neighbors.add(neighborPos);
            }
        }
        return neighbors;
    }

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
