package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;

public final class PowerBlockItem extends SimpleBlockItem {
    public PowerBlockItem() {
        super(BlockRegistry.POWER_BLOCK.get());
    }
}
