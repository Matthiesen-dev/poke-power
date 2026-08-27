package dev.matthiesen.poke_power.common.energy;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class PokeEnergyCable extends AbstractCommonEnergyStorage {
    public PokeEnergyCable(long capacity, long maxExtract) {
        super(capacity, maxExtract);
    }

    @Override
    public void distributeEnergy(Level level, BlockPos blockPos) {
        PokePowerCommon.POWER_TOOLS.distributeEnergy(this, level, blockPos);
    }
}
