package dev.matthiesen.poke_power.common.defs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * NetworkTarget is a record that represents a target in the energy network. It contains the position of the target
 * and the direction from which energy is being transferred.
 *
 * @param pos
 * @param fromSide
 */
public record NetworkTarget(BlockPos pos, Direction fromSide) {
}
