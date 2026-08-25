package dev.matthiesen.poke_power.neoforge;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.PokePowerCommonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = PokePowerCommon.MOD_ID, dist = Dist.CLIENT)
public final class PokePowerNeoForgeClient {
    public static final PokePowerCommonClient INSTANCE = PokePowerCommonClient.INSTANCE;

    public PokePowerNeoForgeClient(IEventBus modBus) {
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerRenderers);
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
    }

    public void clientSetup(FMLClientSetupEvent event) {
        INSTANCE.initialize();
        INSTANCE.registerScreens();
    }

    public void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        INSTANCE.registerRenderers();
    }
}
