package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;
import software.bernie.geckolib.animation.RawAnimation;

public final class PowerBlockItem extends AbstractGeckoItem {
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.power_block.idle");

    public PowerBlockItem() {
        super(IDLE_ANIMATION, BlockRegistry.POWER_BLOCK.get());
    }
}
