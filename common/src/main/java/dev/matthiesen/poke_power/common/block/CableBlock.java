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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CableBlock extends Block implements EntityBlock {
    private static final EnumProperty<ConnectionType> NORTH = EnumProperty.create("north", ConnectionType.class);
    private static final EnumProperty<ConnectionType> SOUTH = EnumProperty.create("south", ConnectionType.class);
    private static final EnumProperty<ConnectionType> EAST = EnumProperty.create("east", ConnectionType.class);
    private static final EnumProperty<ConnectionType> WEST = EnumProperty.create("west", ConnectionType.class);
    private static final EnumProperty<ConnectionType> UP = EnumProperty.create("up", ConnectionType.class);
    private static final EnumProperty<ConnectionType> DOWN = EnumProperty.create("down", ConnectionType.class);

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
                .setValue(NORTH, ConnectionType.NONE)
                .setValue(SOUTH, ConnectionType.NONE)
                .setValue(EAST, ConnectionType.NONE)
                .setValue(WEST, ConnectionType.NONE)
                .setValue(UP, ConnectionType.NONE)
                .setValue(DOWN, ConnectionType.NONE));
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
        ConnectionType northType = getConnectionType(level, pos, Direction.NORTH);
        ConnectionType southType = getConnectionType(level, pos, Direction.SOUTH);
        ConnectionType eastType = getConnectionType(level, pos, Direction.EAST);
        ConnectionType westType = getConnectionType(level, pos, Direction.WEST);
        ConnectionType upType = getConnectionType(level, pos, Direction.UP);
        ConnectionType downType = getConnectionType(level, pos, Direction.DOWN);

        return this.defaultBlockState()
                .setValue(NORTH, northType)
                .setValue(SOUTH, southType)
                .setValue(EAST, eastType)
                .setValue(WEST, westType)
                .setValue(UP, upType)
                .setValue(DOWN, downType);
    }

    // Updates connections automatically when a neighboring block changes or gets broken
    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return state.setValue(getPropertyForDirection(direction), getConnectionType(level, currentPos, direction));
    }

    private ConnectionType getConnectionType(LevelAccessor level, BlockPos currentPos, Direction direction) {
        BlockPos targetPos = currentPos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.getBlock() instanceof CableBlock) {
            return ConnectionType.CABLE;
        }

        if (!(level instanceof Level currentLevel)) {
            return ConnectionType.NONE;
        }

        if (PokePowerCommon.POWER_TOOLS.supportsEnergyTransfer(currentLevel, currentPos, direction)) {
            return ConnectionType.MACHINE;
        }

        return ConnectionType.NONE;
    }

    private boolean isConnected(ConnectionType connectionType) {
        return connectionType != ConnectionType.NONE;
    }

    private EnumProperty<ConnectionType> getPropertyForDirection(Direction direction) {
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

    private enum ConnectionType implements StringRepresentable {
        NONE("none"),
        CABLE("cable"),
        MACHINE("machine");

        private final String serializedName;

        ConnectionType(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }
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
