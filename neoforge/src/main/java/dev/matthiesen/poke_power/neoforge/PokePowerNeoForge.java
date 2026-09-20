package dev.matthiesen.poke_power.neoforge;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(PokePowerCommon.MOD_ID)
public final class PokePowerNeoForge {
    public static final PokePowerCommon INSTANCE = PokePowerCommon.INSTANCE;
    public static volatile IEventBus modBus;

    public PokePowerNeoForge(IEventBus modBus) {
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        PokePowerNeoForge.modBus = modBus;
        INSTANCE.initialize();
    }
}
