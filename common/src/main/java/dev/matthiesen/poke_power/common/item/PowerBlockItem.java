package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;
import net.minecraft.world.item.BlockItem;

public final class PowerBlockItem extends BlockItem {
    public PowerBlockItem() {
        super(BlockRegistry.POWER_BLOCK.get(), new Properties());
    }
}
