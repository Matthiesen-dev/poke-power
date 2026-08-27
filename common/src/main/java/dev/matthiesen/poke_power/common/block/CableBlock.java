package dev.matthiesen.poke_power.common.block;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.CableBlockEntity;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CableBlock extends Block implements EntityBlock {
    private static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    private static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    private static final BooleanProperty EAST = BlockStateProperties.EAST;
    private static final BooleanProperty WEST = BlockStateProperties.WEST;
    private static final BooleanProperty UP = BlockStateProperties.UP;
    private static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE_SHAPE = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_SHAPE = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP_SHAPE = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_SHAPE = Block.box(6, 0, 6, 10, 6, 10);

    public CableBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .strength(4f)
                        .requiresCorrectToolForDrops()
        );
        // Default state is a floating pipe center with no arms connected
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    // Define the properties this block uses
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    // Sets the initial connection states when the player places the block
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return this.defaultBlockState()
                .setValue(NORTH, shouldConnect(level, pos, Direction.NORTH))
                .setValue(SOUTH, shouldConnect(level, pos, Direction.SOUTH))
                .setValue(EAST, shouldConnect(level, pos, Direction.EAST))
                .setValue(WEST, shouldConnect(level, pos, Direction.WEST))
                .setValue(UP, shouldConnect(level, pos, Direction.UP))
                .setValue(DOWN, shouldConnect(level, pos, Direction.DOWN));
    }

    // Updates connections automatically when a neighboring block changes or gets broken
    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        BooleanProperty property = getPropertyForDirection(direction);
        if (property != null) {
            return state.setValue(property, shouldConnect(level, currentPos, direction));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private boolean shouldConnect(LevelAccessor level, BlockPos currentPos, Direction direction) {
        BlockPos targetPos = currentPos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.getBlock() == this) {
            return true;
        }

        if (!(level instanceof Level currentLevel)) {
            return false;
        }

        return PokePowerCommon.POWER_TOOLS.supportsEnergyTransfer(currentLevel, currentPos, direction);
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);
        return shape;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CableBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        if (blockEntityType.equals(BlockEntityRegistry.POWER_CABLE_BE.get())) {
            return CableBlockEntity::tick;
        }
        return null;
    }
}
