package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.block.entity.NeoplasmRotBlockEntity;
import org.jetbrains.annotations.NotNull;

public class NeoplasmVeinBlock extends NeoplasmBlock {
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * NPV - Neoplasm vein
     */
    private static final double NPV_BRANCHING_CHANCE = 0.015;
    private static final float NPV_FALL_CHANCE = 0.75f;
    private static final float NPV_ORIGINAL_DIRECTION_CHANCE = 0.45f;
    private static final int NPV_REROLL_ATTEMPTS = 10;
    private static final int NPV_MIN_TICKS_TO_SPREAD = 140;
    private static final int NPV_MAX_TICKS_TO_SPREAD = 220;

    public NeoplasmVeinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MATURE, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATURE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Only a "young" vein (which is not yet mature) can grow
        if (!state.getValue(MATURE)) {
            performGrowth(level, pos, state, random);
        }
        System.out.println(" tick");
    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.getValue(MATURE)) {
            // If block schedule chain is broken, trying to launch it again
            performGrowth(level, pos, state, random);
            System.out.println("Random tick");
        }
    }

    ///  WIP
    /// Make dynamic tick rate?
    /// Code clearance?
    private void performGrowth(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Direction originalDir = state.getValue(FACING);
        Direction growDir = (random.nextFloat() < NPV_ORIGINAL_DIRECTION_CHANCE) ? originalDir : Direction.getRandom(
                random);
        // Not allowing our vein to go back and get twisted into knots
        if (growDir == originalDir.getOpposite()) return;

        BlockPos targetPos = pos.relative(growDir);

        if (growDir == Direction.UP && hasNoWallNearby(level, targetPos)) return;
        // We're trying to decrease chance of veins contact
        // or increase spread chance, if there is unreplaceable block
        if (hasNeoplasmNearby(level, targetPos, pos) || !NeoplasmUtils.isReplaceable(level.getBlockState(targetPos))) {
            for (int i = 0; i < NPV_REROLL_ATTEMPTS; i++) {
                Direction newDir = Direction.getRandom(random);
                BlockPos newTarget = pos.relative(newDir);

                if (!hasNeoplasmNearby(level, newTarget, pos)) {
                    growDir = newDir;
                    targetPos = newTarget;
                    break;
                }
            }
        }
        if (hasNoWallNearby(level, pos.relative(growDir))) {
            if (random.nextFloat() < NPV_FALL_CHANCE) {
                targetPos = pos.relative(Direction.DOWN);
            }
        }

        BlockState targetState = level.getBlockState(targetPos);
        System.out.println("Selected direcrion: " + growDir);

        NeoplasmUtils.ResourceEntry info = NeoplasmUtils.getResourceInfo(targetState.getBlock());
        if (info.type() != NeoplasmResourceType.NONE) {
            devour(level, pos, state, targetPos, info, targetState);
        }
        else if (NeoplasmUtils.isReplaceable(targetState)) {
            grow(level, pos, state, random, targetPos, originalDir, growDir);
        }
    }

    // Devour
    // TODO(whiteman) make a in radius devouring / improve
    private static void devour(ServerLevel level, BlockPos pos, BlockState state, BlockPos targetPos, NeoplasmUtils.ResourceEntry info, BlockState targetState) {
        level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                .setValue(NeoplasmRotBlock.TYPE, info.type()), 3);

        if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity devourBE) {
            devourBE.setOriginalState(targetState);
        }
    }

    // Grow
    // There a chance to branch neoplasm into new vein
    // by just not setting current vein into mature
    // and changing original grow direction (to spread nor in 1 dir.)
    private void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, BlockPos targetPos, Direction parentDir, Direction growDir) {
        if (random.nextDouble() > NPV_BRANCHING_CHANCE) {
            // Target block
            level.setBlock(targetPos, this.defaultBlockState().setValue(FACING, parentDir), 3);
            level.scheduleTick(targetPos, this, Math.min((NPV_MIN_TICKS_TO_SPREAD + random.nextInt(NPV_MAX_TICKS_TO_SPREAD)), NPV_MAX_TICKS_TO_SPREAD));
            // Current block
            level.setBlock(pos, state.setValue(MATURE, true), 3);
        } else {
            Direction nextDir;
            do {
                nextDir = Direction.getRandom(random);
            } while (nextDir.getAxis() == growDir.getAxis());
            // Only target block
            level.setBlock(targetPos, this.defaultBlockState().setValue(FACING, nextDir), 3);
            level.scheduleTick(targetPos, this, Math.min((NPV_MIN_TICKS_TO_SPREAD + random.nextInt(NPV_MAX_TICKS_TO_SPREAD)), NPV_MAX_TICKS_TO_SPREAD));
        }
    }

    private boolean hasNoWallNearby(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos adj = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(adj);

                    if (!(state.getBlock() instanceof NeoplasmBlock)) {
                        if (state.isCollisionShapeFullBlock(level, adj)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasNeoplasmNearby(Level level, BlockPos targetPos, BlockPos currentPos) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = targetPos.relative(d);
            if (!neighbor.equals(currentPos) && level.getBlockState(neighbor).getBlock() instanceof NeoplasmBlock) {
                return true;
            }
        }
        return false;
    }
}
