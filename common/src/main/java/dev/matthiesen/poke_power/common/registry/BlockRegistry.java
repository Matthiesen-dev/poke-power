package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.CableBlock;
import dev.matthiesen.poke_power.common.block.PowerBlock;

import java.util.function.Supplier;

public final class BlockRegistry extends AbstractBlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();

    private BlockRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<PowerBlock> POWER_BLOCK;
    public static final Supplier<CableBlock> POWER_CABLE;

    static {
        POWER_BLOCK = INSTANCE.register("power_block", PowerBlock::new);
        POWER_CABLE = INSTANCE.register("power_cable", CableBlock::new);
    }
}
