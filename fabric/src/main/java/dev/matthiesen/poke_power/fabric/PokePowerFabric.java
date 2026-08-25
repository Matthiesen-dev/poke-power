package dev.matthiesen.poke_power.fabric;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.fabricmc.api.ModInitializer;

public final class PokePowerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var instance = PokePowerCommon.INSTANCE;
        instance.createInfoLog("Loading for Fabric Mod Loader");
        instance.initialize();
    }
}
