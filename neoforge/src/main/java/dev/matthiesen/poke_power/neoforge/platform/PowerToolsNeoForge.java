package dev.matthiesen.poke_power.neoforge.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.neoforge.api.energy.NeoForgeEnergyWrapper;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.Supplier;

public final class PowerToolsNeoForge implements PowerTools {
    @Override
    public void registerEnergyCapability(Supplier<BlockEntityType<?>> blockEntityTypeSupplier) {
        NeoForge.EVENT_BUS.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                blockEntityTypeSupplier.get(),
                (blockEntity, side) -> {
                    if (blockEntity instanceof PowerBlockEntity energyBlock) {
                        return new NeoForgeEnergyWrapper(energyBlock.getEnergyStorage());
                    }
                    return null;
                }
        ));
    }

    @Override
    public void distributeEnergy(AbstractCommonEnergyStorage storage, Level level, BlockPos pos) {
        int availableEnergy = (int) storage.getEnergy();
        int maxTransfer = Math.toIntExact(Math.min(availableEnergy, storage.getMaxExtract()));

        for (Direction direction : Direction.values()) {
            if (maxTransfer <= 0) break;

            BlockPos neighborPos = pos.relative(direction);
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());

            if (targetStorage != null && targetStorage.canReceive()) {
                int accepted = targetStorage.receiveEnergy(maxTransfer, false);
                if (accepted > 0) {
                    storage.setEnergy(storage.getEnergy() - accepted);
                    maxTransfer -= accepted;
                }
            }
        }
    }
}
