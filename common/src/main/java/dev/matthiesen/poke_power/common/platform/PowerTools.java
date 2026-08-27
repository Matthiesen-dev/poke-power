package dev.matthiesen.poke_power.common.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.IPowerPlatformBase;
import dev.matthiesen.matthiesen_core.common.api.energy.IPowerPlatformItems;
import dev.matthiesen.poke_power.common.block.entity.CableBlockEntity;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface PowerTools extends IPowerPlatformBase, IPowerPlatformItems {

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
