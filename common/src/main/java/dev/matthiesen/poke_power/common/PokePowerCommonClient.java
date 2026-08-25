package dev.matthiesen.poke_power.common;

import dev.matthiesen.matthiesen_core.common.AbstractCommonClientMod;
import dev.matthiesen.poke_power.common.client.geckolib.PowerBlockRenderer;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.registry.ItemRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class PokePowerCommonClient extends AbstractCommonClientMod {
    public static final PokePowerCommonClient INSTANCE = new PokePowerCommonClient();

    public PokePowerCommonClient() {
        super(PokePowerCommon.INSTANCE);
    }

    @Override
    public void initialize() {
        createInfoLog("Initializing client");
    }

    public void registerRenderers() {
        // Item Entity Renderers
        ItemRegistry.POWER_BLOCK.get().renderProviderHolder.setValue(makeRendererProvider(PowerBlockRenderer.INSTANCE.getItemRenderer()));

        // Block Entity Renderers
        INSTANCE.getEntityRendererManager().registerBlockEntityRenderer(BlockEntityRegistry.POWER_BLOCK_BE.get(), context -> PowerBlockRenderer.INSTANCE.getBlockRenderer());
    }

    private static <T extends Item & GeoItem> GeoRenderProvider makeRendererProvider(GeoItemRenderer<T> renderer) {
        return new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer itemRenderer;

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = renderer;
                }
                return this.itemRenderer;
            }
        };
    }
}
