package dev.matthiesen.poke_power.common.client.geckolib;

import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.client.geckolib.abstracts.AbstractGeoBlockRenderer;
import dev.matthiesen.poke_power.common.item.PowerBlockItem;

public final class PowerBlockRenderer extends AbstractGeoBlockRenderer<PowerBlockEntity, PowerBlockItem> {
    public PowerBlockRenderer() {
        super("power_block", true, false);
    }

    public static final PowerBlockRenderer INSTANCE = new PowerBlockRenderer();
}
