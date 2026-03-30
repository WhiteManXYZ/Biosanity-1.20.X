package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.block.entity.ModBlockEntities;
import net.whiteman.biosanity.block.entity.custom.NeoplasmRotBlockEntity;
import net.whiteman.biosanity.block.entity.custom.NeoplasmVeinBlockEntity;
import net.whiteman.biosanity.util.block.NeoplasmUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.whiteman.biosanity.util.block.NeoplasmUtils.ResourceRegistry.*;

public class NeoplasmVeinBlock extends BaseEntityBlock implements INeoplasmNode {
    // States
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty PARENT_DIRECTION = DirectionProperty.create("parent_direction", Direction.values());
    public static final BooleanProperty HAS_NUTRIENT = BooleanProperty.create("has_nutrient");
    // Base params
    private static final double BRANCHING_CHANCE = 0.02;
    private static final double FALL_CHANCE = 0.75;
    private static final double ORIGINAL_DIRECTION_CHANCE = 0.45;
    private static final int REROLL_ATTEMPTS = 10;
    private static final double MATURE_CHANCE = 0.004;
    // Tick rate params
    private static final int MIN_TICKS_TO_SPREAD = 140;
    private static final int MAX_TICKS_TO_SPREAD = 400;
    private static final double RAIN_WEATHER_SPREAD_MODIFIER = 0.9;
    private static final double NIGHT_SPREAD_MODIFIER = 0.8;

    public NeoplasmVeinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MATURE, false)
                .setValue(HAS_NUTRIENT, false));
    }

    private void scheduleNextTick(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            double delay = level.random.nextInt(MIN_TICKS_TO_SPREAD, MAX_TICKS_TO_SPREAD);
            // We apply spread speed modifiers for variable gameplay
            // and don't allow delay to be lower than 20 ticks
            level.scheduleTick(pos, this, Math.max(spreadSpeedModifiers(level, delay), 20));
        }
    }

    /// WIP
    /// Maybe make smart resource searching?
    private void performGrowth(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        // There a chance to just grow up and end vein chain
        if (random.nextDouble() < MATURE_CHANCE) {
            level.setBlock(pos, state.setValue(MATURE, true), 3);
            return;
        }
        // Original dir contains original "root vein" direction
        Direction originalDir = state.getValue(FACING);
        Direction growDir = (random.nextDouble() < ORIGINAL_DIRECTION_CHANCE) ? originalDir : Direction.getRandom(random);

        // Our special conditions to better vein spread
        if (!conditions(level, pos, originalDir, growDir)) { return; }

        // We're trying to decrease chance of veins contact
        // or increase spread chance, if there is unreplaceable block
        if (hasNeoplasmNearby(level, pos.relative(growDir), pos) || !isReplaceable(level.getBlockState(pos.relative(growDir)))) {
            for (int i = 0; i < REROLL_ATTEMPTS; i++) {
                Direction newDir = Direction.getRandom(random);

                if (!hasNeoplasmNearby(level, pos.relative(newDir), pos)) {
                    growDir = newDir;
                    break;
                }
            }
        }

        // Increase vein chance to grow downwards when there is no wall
        if (hasNoWallNearby(level, pos.relative(growDir))) {
            if (random.nextDouble() < FALL_CHANCE) {
                growDir = Direction.DOWN;
            }
        }

        // Deciding what vein supposed to do:
        // Spread or Absorb resource
        ResourceResult nearbyResource = findResourceNearby(level, pos);
        // Infection resource blocks is more important than spread
        if (nearbyResource != null) {
            absorbResources(level, pos.relative(nearbyResource.direction), nearbyResource.info, nearbyResource.state);
        }
        else {
            BlockState targetState = level.getBlockState(pos.relative(growDir));
            if (NeoplasmUtils.ResourceRegistry.isReplaceable(targetState)) {
                grow(level, pos, state, random, growDir, originalDir);
            }
        }
    }

    // Absorb
    // Creates a "patient-zero" absorbed resource
    // that continue spread rot blocks by himself
    private void absorbResources(ServerLevel level, BlockPos targetPos, ResourceTypeEntry info, BlockState targetState) {
        level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                .setValue(NeoplasmRotBlock.RESOURCE_TYPE, info.resourceType())
                .setValue(NeoplasmRotBlock.LEVEL, info.level()), 3);

        if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity devourBE) {
            devourBE.setOriginalState(targetState);
        }
    }

    // Grow
    // Has a chance to "split" in different directions
    // by just not setting current vein into mature
    // and changing original grow direction (to spread nor in 1 dir.)
    private void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, Direction growDir, Direction originalDir) {
        BlockPos targetPos = pos.relative(growDir);
        if (random.nextDouble() > BRANCHING_CHANCE) {
            // Target block
            level.setBlock(targetPos, this.defaultBlockState()
                    .setValue(PARENT_DIRECTION, growDir.getOpposite())
                    .setValue(FACING, originalDir), 3);
            scheduleNextTick(level, targetPos);
            // Current block
            level.setBlock(pos, state.setValue(MATURE, true), 3);
        } else if (!targetPos.relative(originalDir.getOpposite()).equals(pos)) {
            Direction nextDir = calculateOriginalDirection(pos, random, targetPos, originalDir);
            if (nextDir == null) return;
            // Only target block
            level.setBlock(targetPos, this.defaultBlockState()
                    .setValue(PARENT_DIRECTION, growDir.getOpposite())
                    .setValue(FACING, nextDir), 3);
            scheduleNextTick(level, targetPos);
        }
    }

    private static Direction calculateOriginalDirection(BlockPos pos, RandomSource random, BlockPos targetPos, Direction originalDir) {
        // Branch direction calculator
        Direction branchDir = null;
        Direction nextDir;
        // Calculating relative coordinates
        for (Direction dir : Direction.values()) {
            if (targetPos.relative(dir.getOpposite()).equals(pos)) {
                branchDir = dir;
                break;
            }
        }
        if (branchDir == null) return null;

        // Don't allow to set facing vertical
        // or opposite from original direction
        if (branchDir.getAxis().isVertical()) {
            do {
                branchDir = Direction.getRandom(random);
            } while (branchDir.getAxis().isVertical() || branchDir == originalDir.getOpposite());
        }
        nextDir = branchDir;
        return nextDir;
    }

    private static boolean conditions(Level level, BlockPos pos, Direction originalDir, Direction growDir) {
        // Don't allow to grow backwards
        if (growDir == originalDir.getOpposite()) return false;
        // Don't allow to climb up without wall
        if (growDir == Direction.UP && hasNoWallNearby(level, pos.relative(growDir))) return false;

        return true;
    }

    private static int spreadSpeedModifiers(Level level, double delay) {
        if (level.isRaining()) delay *= RAIN_WEATHER_SPREAD_MODIFIER;
        if (level.isNight()) delay *= NIGHT_SPREAD_MODIFIER;
        return (int) Math.ceil(delay);
    }

    private static boolean hasNoWallNearby(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos adj = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(adj);

                    if (!(state.getBlock() instanceof INeoplasmNode)) {
                        if (state.isCollisionShapeFullBlock(level, adj)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasNeoplasmNearby(Level level, BlockPos targetPos, BlockPos currentPos) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = targetPos.relative(d);
            if (!neighbor.equals(currentPos) && level.getBlockState(neighbor).getBlock() instanceof INeoplasmNode) {
                return true;
            }
        }
        return false;
    }

    private record ResourceResult(Direction direction, ResourceTypeEntry info, BlockState state) {}

    private static ResourceResult findResourceNearby(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos checkPos = pos.relative(d);
            BlockState state = level.getBlockState(checkPos);
            ResourceTypeEntry info = getResourceInfo(state.getBlock());

            if (info.resourceType().isResource()) {
                return new ResourceResult(d, info, state);
            }
        }
        return null;
    }

    private static boolean hasNonMatureNearby(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos checkPos = pos.relative(d);
            BlockState state = level.getBlockState(checkPos);

            if (state.hasProperty(MATURE) && !state.getValue(MATURE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmVeinBlockEntity(pos, state);
    }

    @Override
    public void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(HAS_NUTRIENT)) {
            if (level.getBlockEntity(pos) instanceof NeoplasmVeinBlockEntity blockEntity) {
                blockEntity.tick(level, pos, state, blockEntity);
            }
        }
        // Only a "young" vein (which is not yet mature) can grow
        if (!state.getValue(MATURE)) {
            performGrowth(level, pos, state, random);
        }
        scheduleNextTick(level, pos);
    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.getValue(MATURE)) {
            // Random tick for faster spread and
            // if block schedule chain is broken, trying to launch it again
            performGrowth(level, pos, state, random);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        // If the living end of our vein is destroyed, we make the previous block young again
        // (and all neighbors too)
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if ((state.hasProperty(MATURE) && !state.getValue(MATURE)) || hasNonMatureNearby(level, pos)) {
                if (neighborState.hasProperty(MATURE) && neighborState.getValue(MATURE)) {
                    level.setBlock(neighborPos, neighborState.setValue(MATURE, false), 3);

                    // Test particle
                    level.levelEvent(2001, neighborPos, Block.getId(neighborState));
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATURE, PARENT_DIRECTION, HAS_NUTRIENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleNextTick(level, pos);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.NEOPLASM_VEIN_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1, pBlockEntity));
    }
}
