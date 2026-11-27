package dev.matthiesen.poke_power.common.block;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.CableBlockEntity;
import dev.matthiesen.poke_power.common.defs.CableConnectionType;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CableBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<CableConnectionType> NORTH = EnumProperty.create("north", CableConnectionType.class);
    private static final EnumProperty<CableConnectionType> SOUTH = EnumProperty.create("south", CableConnectionType.class);
    private static final EnumProperty<CableConnectionType> EAST = EnumProperty.create("east", CableConnectionType.class);
    private static final EnumProperty<CableConnectionType> WEST = EnumProperty.create("west", CableConnectionType.class);
    private static final EnumProperty<CableConnectionType> UP = EnumProperty.create("up", CableConnectionType.class);
    private static final EnumProperty<CableConnectionType> DOWN = EnumProperty.create("down", CableConnectionType.class);

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
        this.registerDefaultState(
                this.stateDefinition.any()
                .setValue(NORTH, CableConnectionType.NONE)
                .setValue(SOUTH, CableConnectionType.NONE)
                .setValue(EAST, CableConnectionType.NONE)
                .setValue(WEST, CableConnectionType.NONE)
                .setValue(UP, CableConnectionType.NONE)
                .setValue(DOWN, CableConnectionType.NONE)
                .setValue(WATERLOGGED, false)
        );
    }

    // Define the properties this block uses
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
    }

    // Sets the initial connection states when the player places the block
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        CableConnectionType northType = getConnectionType(level, pos, Direction.NORTH);
        CableConnectionType southType = getConnectionType(level, pos, Direction.SOUTH);
        CableConnectionType eastType = getConnectionType(level, pos, Direction.EAST);
        CableConnectionType westType = getConnectionType(level, pos, Direction.WEST);
        CableConnectionType upType = getConnectionType(level, pos, Direction.UP);
        CableConnectionType downType = getConnectionType(level, pos, Direction.DOWN);
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());

        return this.defaultBlockState()
                .setValue(NORTH, northType)
                .setValue(SOUTH, southType)
                .setValue(EAST, eastType)
                .setValue(WEST, westType)
                .setValue(UP, upType)
                .setValue(DOWN, downType)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    // Updates connections automatically when a neighboring block changes or gets broken
    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.setValue(getPropertyForDirection(direction), getConnectionType(level, currentPos, direction));
    }

    private CableConnectionType getConnectionType(LevelAccessor level, BlockPos currentPos, Direction direction) {
        BlockPos targetPos = currentPos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.getBlock() instanceof CableBlock) {
            return CableConnectionType.CABLE;
        }

        if (!(level instanceof Level currentLevel)) {
            return CableConnectionType.NONE;
        }

        if (PokePowerCommon.POWER_TOOLS.supportsEnergyTransfer(currentLevel, currentPos, direction)) {
            return CableConnectionType.MACHINE;
        }

        return CableConnectionType.NONE;
    }

    private boolean isConnected(CableConnectionType connectionType) {
        return connectionType != CableConnectionType.NONE;
    }

    private EnumProperty<CableConnectionType> getPropertyForDirection(Direction direction) {
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
        if (isConnected(state.getValue(NORTH))) shape = Shapes.or(shape, NORTH_SHAPE);
        if (isConnected(state.getValue(SOUTH))) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (isConnected(state.getValue(EAST))) shape = Shapes.or(shape, EAST_SHAPE);
        if (isConnected(state.getValue(WEST))) shape = Shapes.or(shape, WEST_SHAPE);
        if (isConnected(state.getValue(UP))) shape = Shapes.or(shape, UP_SHAPE);
        if (isConnected(state.getValue(DOWN))) shape = Shapes.or(shape, DOWN_SHAPE);
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
