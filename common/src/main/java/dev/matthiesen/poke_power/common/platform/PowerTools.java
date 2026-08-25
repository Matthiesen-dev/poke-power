package dev.matthiesen.poke_power.common.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface PowerTools {
    void registerEnergyCapability(Supplier<BlockEntityType<?>> blockEntityTypeSupplier);
    void distributeEnergy(AbstractCommonEnergyStorage storage, Level level, BlockPos pos);
}
