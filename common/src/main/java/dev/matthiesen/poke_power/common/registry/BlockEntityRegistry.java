package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractBlockEntityRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class BlockEntityRegistry extends AbstractBlockEntityRegistry {
    public static final BlockEntityRegistry INSTANCE = new BlockEntityRegistry();

    private BlockEntityRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<BlockEntityType<PowerBlockEntity>> POWER_BLOCK_BE;

    static {
        POWER_BLOCK_BE = INSTANCE.register("power_block", PowerBlockEntity::new, BlockRegistry.POWER_BLOCK);

        PokePowerCommon.POWER_TOOLS.registerEnergyCapability(POWER_BLOCK_BE::get);
    }
}
