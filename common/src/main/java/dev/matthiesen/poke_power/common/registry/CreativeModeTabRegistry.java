package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractCreativeModeTabRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public final class CreativeModeTabRegistry extends AbstractCreativeModeTabRegistry {
    public static final CreativeModeTabRegistry INSTANCE = new CreativeModeTabRegistry();

    private CreativeModeTabRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {
    }

    public static final Supplier<CreativeModeTab> POKE_POWER_TAB;

    static {
        POKE_POWER_TAB = INSTANCE.registerSimpleCreativeTab(
                "poke_power_tab",
                Component.literal("Poke Power"),
                ItemRegistry.getCreativeModeTabIcon(),
                ItemRegistry.getCreativeModeTabItems()
        );
    }
}
