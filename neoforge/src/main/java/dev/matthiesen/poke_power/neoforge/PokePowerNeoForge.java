package dev.matthiesen.poke_power.neoforge;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.neoforged.fml.common.Mod;

@Mod(PokePowerCommon.MOD_ID)
public final class PokePowerNeoForge {
    public static final PokePowerCommon INSTANCE = PokePowerCommon.INSTANCE;

    public PokePowerNeoForge() {
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        INSTANCE.initialize();
    }
}
