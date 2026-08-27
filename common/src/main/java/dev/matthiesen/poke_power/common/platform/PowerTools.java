package dev.matthiesen.poke_power.common.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.poke_power.common.block.entity.CableBlockEntity;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface PowerTools {
    /**
     * Registers the energy capability for the given block entity type.
     *
     * @param blockEntityTypeSupplier a supplier that provides the block entity type to register the energy capability for
     */
    void registerEnergyCapability(Supplier<BlockEntityType<?>> blockEntityTypeSupplier);

    /**
     * Distributes energy from the given {@code storage} to adjacent blocks in the {@code level} at the given {@code pos}.
     *
     * @param storage the energy storage to distribute energy from
     * @param level the level to distribute energy in
     * @param pos the position of the block to distribute energy from
     */
    void distributeEnergy(AbstractCommonEnergyStorage storage, Level level, BlockPos pos);

    /**
     * Checks if the block at {@code pos} in {@code level} supports energy transfer in the given {@code direction}.
     *
     * @param level the level to check
     * @param pos the position of the block to check
     * @param direction the direction to check for energy transfer
     * @return true if the block supports energy transfer in the given direction, false otherwise
     */
    boolean supportsEnergyTransfer(Level level, BlockPos pos, Direction direction);

    /**
     * Attempts to push up to {@code maxAmount} energy into the block at {@code targetPos},
     * queried from {@code fromSide} (the face of the target block that faces the cable).
     *
     * @return the amount actually inserted
     */
    long pushEnergyTo(long maxAmount, Level level, BlockPos targetPos, Direction fromSide);

    /**
     * Checks whether the given stack can receive energy.
     */
    boolean canChargeItem(ItemStack stack);

    /**
     * Attempts to insert up to {@code maxAmount} energy into the given item stack.
     *
     * @return the amount actually inserted
     */
    long chargeItem(ItemStack stack, long maxAmount);

    /**
     * Checks if the given block entity is a PokePower block entity (either a PowerBlockEntity or CableBlockEntity).
     *
     * @param blockEntity the block entity to check
     * @return true if the block entity is a PokePower block entity, false otherwise
     */
    default boolean isPokePowerBlockEntity(BlockEntity blockEntity) {
        if (blockEntity instanceof PowerBlockEntity) {
            return true;
        }
        return blockEntity instanceof CableBlockEntity;
    }
}
