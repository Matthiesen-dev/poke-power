package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;
import net.minecraft.world.item.BlockItem;

public final class CableBlockItem extends BlockItem {
    public CableBlockItem() {
        super(BlockRegistry.POWER_CABLE.get(), new Properties());
    }
}
