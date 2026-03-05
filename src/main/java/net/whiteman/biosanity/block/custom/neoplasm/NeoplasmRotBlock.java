package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
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
import net.whiteman.biosanity.block.entity.NeoplasmRotBlockEntity;
import net.whiteman.biosanity.item.ModItems;
import org.jetbrains.annotations.NotNull;

public class NeoplasmRotBlock extends NeoplasmBlock implements EntityBlock {
    public static final EnumProperty<NeoplasmResourceType> TYPE = EnumProperty.create("type", NeoplasmResourceType.class);
    private static final double DECREASE_DROP_CHANCE_RATE = 0.2;

    public NeoplasmRotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, NeoplasmResourceType.NONE));
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
        if (pLevel.getBlockEntity(pPos) instanceof NeoplasmRotBlockEntity be) {
            be.decreaseChance(DECREASE_DROP_CHANCE_RATE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @org.jetbrains.annotations.Nullable BlockEntity pBlockEntity, @NotNull ItemStack pTool) {
        if (pBlockEntity instanceof NeoplasmRotBlockEntity be) {
            BlockState original = be.getOriginalState();

            if (!player.isCreative()) {
                double currentChance = be.getDropChance();
                if (level.random.nextFloat() < currentChance && !original.isAir()) {
                    Block.popResource(level, pos, new ItemStack(original.getBlock().asItem()));
                } else {
                    Block.popResource(level, pos, new ItemStack(ModItems.NEOPLASM_ROT.get()));
                }
            }
        }
        super.playerDestroy(level, player, pos, state, pBlockEntity, pTool);
    }
}
