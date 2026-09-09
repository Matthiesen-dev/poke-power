package dev.matthiesen.poke_power.common;

import dev.matthiesen.matthiesen_core.common.AbstractCommonClientMod;
import dev.matthiesen.poke_power.common.client.renderer.PowerBlockEntityRenderer;
import dev.matthiesen.poke_power.common.client.screen.PowerBlockScreen;
import dev.matthiesen.poke_power.common.network.SyncGeneratorPayload;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.registry.MenuRegistry;

public final class PokePowerCommonClient extends AbstractCommonClientMod {
    public static final PokePowerCommonClient INSTANCE = new PokePowerCommonClient();

    public PokePowerCommonClient() {
        super(PokePowerCommon.INSTANCE);
    }

    @Override
    public void initialize() {
        createInfoLog("Initializing client");

        // Register the client-bound (S2C) sync packet handler
        PokePowerCommon.INSTANCE.getNetworkingManager()
                .registerS2C(SyncGeneratorPayload.TYPE, SyncGeneratorPayload.CODEC, SyncGeneratorPayload::handleClient);
    }

    public void registerRenderers() {
        INSTANCE.getEntityRendererManager().registerBlockEntityRenderer(BlockEntityRegistry.POWER_BLOCK_BE, PowerBlockEntityRenderer::new);
    }

    public void registerScreens() {
        INSTANCE.getScreenManager().registerMenuScreen(MenuRegistry.POWER_GENERATOR_MENU, PowerBlockScreen::new);
    }
}
