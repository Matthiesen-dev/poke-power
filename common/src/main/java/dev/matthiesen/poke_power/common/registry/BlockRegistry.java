package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.PowerBlock;

import java.util.function.Supplier;

public final class BlockRegistry extends AbstractBlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();

    private BlockRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<PowerBlock> POWER_BLOCK;

    static {
        POWER_BLOCK = INSTANCE.register("power_block", PowerBlock::new);
    }
}
