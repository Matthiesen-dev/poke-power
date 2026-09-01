package dev.matthiesen.poke_power.common.energy;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyGenerator;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.config.PokePowerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class PokeEnergyGenerator extends AbstractCommonEnergyGenerator {
    public PokeEnergyGenerator() {
        super(PokePowerConfig.SERVER_CONFIG.blocks_powerBlock_capacity.get(), PokePowerConfig.SERVER_CONFIG.blocks_powerBlock_maxExtract.get());
    }

    @Override
    public void distributeEnergy(Level level, BlockPos blockPos) {
        PokePowerCommon.POWER_TOOLS.distributeEnergy(this, level, blockPos);
    }
}
