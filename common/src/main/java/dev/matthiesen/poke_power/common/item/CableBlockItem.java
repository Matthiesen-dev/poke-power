package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;

public final class CableBlockItem extends SimpleBlockItem {
    public CableBlockItem() {
        super(BlockRegistry.POWER_CABLE.get());
    }
}
