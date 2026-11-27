package dev.matthiesen.poke_power.common.block.entity;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.poke_power.common.energy.EnergyNetworkTransfer;
import dev.matthiesen.poke_power.common.energy.PokeEnergyCable;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class CableBlockEntity extends AbstractEnergyBlockEntity {

    private final PokeEnergyCable energyStorage;

    public CableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.POWER_CABLE_BE.get(), blockPos, blockState);
        energyStorage = new PokeEnergyCable();
    }

    @Override
    public AbstractCommonEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private static final Map<BlockPos, EnergyNetworkTransfer> networkTransfers = new HashMap<>();

    private static EnergyNetworkTransfer getNetworkTransfer(Level level, BlockPos blockPos, BlockState blockState, long maxTransfer, CableBlockEntity cable) {
        return networkTransfers.computeIfAbsent(blockPos, pos -> new EnergyNetworkTransfer(
                level,
                blockPos,
                blockState,
                maxTransfer,
                cable,
                CableBlockEntity::setChanged
        ));
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (!(blockEntity instanceof CableBlockEntity cable)) return;
        if (level.isClientSide) return;

        long available = cable.energyStorage.getEnergy();
        if (available <= 0) {
            setChanged(level, blockPos, blockState);
            return;
        }

        long maxTransfer = Math.min(available, cable.energyStorage.getMaxExtract());

        EnergyNetworkTransfer networkTransfer = getNetworkTransfer(level, blockPos, blockState, maxTransfer, cable);
        networkTransfer.run();
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
    }
}

