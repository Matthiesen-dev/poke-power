package dev.matthiesen.poke_power.fabric.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.matthiesen_core.fabric.api.energy.FabricEnergyWrapper;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Supplier;

public final class PowerToolsFabric implements PowerTools {
    @Override
    public void registerEnergyCapability(Supplier<BlockEntityType<?>> blockEntityTypeSupplier) {
        EnergyStorage.SIDED.registerForBlockEntities((blockEntity, side) -> {
            if (blockEntity instanceof AbstractEnergyBlockEntity energyBlock && isPokePowerBlockEntity(blockEntity)) {
                return new FabricEnergyWrapper(energyBlock.getEnergyStorage());
            }
            return null;
        }, blockEntityTypeSupplier.get());
    }

    @Override
    public void distributeEnergy(AbstractCommonEnergyStorage storage, Level level, BlockPos pos) {
        long availableEnergy = storage.getEnergy();
        long maxTransfer = Math.min(availableEnergy, storage.getMaxExtract());

        for (Direction direction : Direction.values()) {
            if (maxTransfer <= 0) break;

            BlockPos neighborPos = pos.relative(direction);
            EnergyStorage targetStorage = EnergyStorage.SIDED.find(level, neighborPos, direction.getOpposite());

            if (targetStorage != null && targetStorage.supportsInsertion()) {
                // Open an outer transaction to push energy safely
                try (Transaction transaction = Transaction.openOuter()) {
                    long inserted = targetStorage.insert(maxTransfer, transaction);
                    if (inserted > 0) {
                        transaction.commit(); // Finalize the transfer on the target
                        storage.setEnergy(storage.getEnergy() - inserted);
                        maxTransfer -= inserted;
                    }
                }
            }
        }
    }

    @Override
    public boolean supportsEnergyTransfer(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        EnergyStorage targetStorage = EnergyStorage.SIDED.find(level, neighborPos, direction.getOpposite());
        return targetStorage != null && (targetStorage.supportsInsertion() || targetStorage.supportsExtraction());
    }

    @Override
    public long pushEnergyTo(long maxAmount, Level level, BlockPos targetPos, Direction fromSide) {
        EnergyStorage targetStorage = EnergyStorage.SIDED.find(level, targetPos, fromSide);
        if (targetStorage == null || !targetStorage.supportsInsertion()) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = targetStorage.insert(maxAmount, transaction);
            if (inserted > 0) transaction.commit();
            return inserted;
        }
    }

    @Override
    public boolean canChargeItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        EnergyStorage energyStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        return energyStorage != null && energyStorage.supportsInsertion();
    }

    @Override
    public long chargeItem(ItemStack stack, long maxAmount) {
        if (stack.isEmpty() || maxAmount <= 0) return 0;
        EnergyStorage energyStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (energyStorage == null || !energyStorage.supportsInsertion()) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = energyStorage.insert(maxAmount, transaction);
            if (inserted > 0) transaction.commit();
            return inserted;
        }
    }
}
