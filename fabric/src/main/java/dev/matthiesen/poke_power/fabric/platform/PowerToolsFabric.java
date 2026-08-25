package dev.matthiesen.poke_power.fabric.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.fabric.api.energy.FabricEnergyWrapper;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Supplier;

public final class PowerToolsFabric implements PowerTools {
    @Override
    public void registerEnergyCapability(Supplier<BlockEntityType<?>> blockEntityTypeSupplier) {
        EnergyStorage.SIDED.registerForBlockEntities((blockEntity, side) -> {
            if (blockEntity instanceof PowerBlockEntity energyBlock) {
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
}
