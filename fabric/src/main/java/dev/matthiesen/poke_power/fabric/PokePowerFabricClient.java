package dev.matthiesen.poke_power.fabric;

import dev.matthiesen.poke_power.common.PokePowerCommonClient;
import net.fabricmc.api.ClientModInitializer;

public final class PokePowerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var instance = PokePowerCommonClient.INSTANCE;
        instance.createInfoLog("Loading for Fabric Mod Loader (Client)");
        instance.initialize();
        instance.registerRenderers();
        instance.registerScreens();
    }
}
