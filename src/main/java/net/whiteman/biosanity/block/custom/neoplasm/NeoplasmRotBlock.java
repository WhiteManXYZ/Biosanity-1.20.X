package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.block.entity.NeoplasmRotBlockEntity;
import net.whiteman.biosanity.item.ModItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class NeoplasmRotBlock extends NeoplasmBlock implements EntityBlock {
    public static final EnumProperty<NeoplasmResourceType> TYPE = EnumProperty.create("type", NeoplasmResourceType.class);

    private static final int MIN_INFECTION_SPEED = 150;
    private static final int MAX_INFECTION_SPEED = 240;
    private static final double NEOPLASM_ROT_DROP_CHANCE = 0.2;

    public NeoplasmRotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, NeoplasmResourceType.NONE));
    }


    private void scheduleNextTick(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            int delay = level.random.nextInt(MIN_INFECTION_SPEED, MAX_INFECTION_SPEED);
            level.scheduleTick(pos, this, delay);
        }
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleNextTick(level, pos);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Infect 1 block in random direction only if
        // this block is in devour map
        Direction[] directions = Direction.values();
        Direction randomDir = directions[random.nextInt(directions.length)];

        BlockPos targetPos = pos.relative(randomDir);
        BlockState targetState = level.getBlockState(targetPos);

        NeoplasmUtils.ResourceEntry info = NeoplasmUtils.getResourceInfo(targetState.getBlock());
        if (info.type() != NeoplasmResourceType.NONE) {
            level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                    .setValue(NeoplasmRotBlock.TYPE, info.type()), 3);

            if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity be) {
                be.setOriginalState(targetState);
            }
        }

        scheduleNextTick(level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmRotBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        // Rotting over time
        if (pLevel.getBlockEntity(pPos) instanceof NeoplasmRotBlockEntity be) {
            int currentStage = be.getOverlayStage();
            if (currentStage < NeoplasmRotBlockEntity.MAX_STAGES - 1) {
                be.setInfectionStage(currentStage + 1);
            }
        }
        // If block schedule chain is broken, trying to launch it again
        if (!pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            pLevel.scheduleTick(pPos, this, 1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity pBlockEntity, @NotNull ItemStack pTool) {
        if (pBlockEntity instanceof NeoplasmRotBlockEntity be && level instanceof ServerLevel) {
            BlockState original = be.getOriginalState();

            if (!player.isCreative()) {
                double currentChance = be.getCurrentDropChance();
                if (level.random.nextDouble() < currentChance && (!original.isAir() && player.hasCorrectToolForDrops(original))) {
                    // We want the broken block to use a drop table, not the block itself
                    // If player has correct tool for block
                    LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.BLOCK_STATE, original)
                            .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                            .withParameter(LootContextParams.THIS_ENTITY, player);

                    List<ItemStack> drops = original.getBlock().getDrops(original, builder);

                    for (ItemStack stack : drops) {
                        Block.popResource(level, pos, stack);
                    }

                } else if (level.random.nextDouble() < NEOPLASM_ROT_DROP_CHANCE) {
                    Block.popResource(level, pos, new ItemStack(ModItems.NEOPLASM_ROT.get()));
                }
            }
        }
        super.playerDestroy(level, player, pos, state, pBlockEntity, pTool);
    }
}
