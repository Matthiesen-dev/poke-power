package dev.matthiesen.poke_power.common.defs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * BlockChangeCallback is a functional interface that defines a callback method to be invoked when a block change occurs.
 * It accepts the level, block position, and block state as parameters.
 */
@FunctionalInterface
public interface BlockChangeCallback {
    /**
     * Accepts the level, block position, and block state as parameters.
     *
     * @param level      The level in which the block change occurred.
     * @param blockPos   The position of the block that changed.
     * @param blockState The new state of the block after the change.
     */
    void accept(@NotNull Level level, BlockPos blockPos, @NotNull BlockState blockState);
}
