package dev.matthiesen.poke_power.common.client.geckolib.abstracts;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;

public class AbstractGeoBlockRenderer<B extends BlockEntity & GeoAnimatable, I extends Item & GeoAnimatable> {
    private final GeoBlockRenderer<B> blockRenderer;
    private final GeoItemRenderer<I> itemRenderer;

    public AbstractGeoBlockRenderer(String name, boolean isTransparent, boolean isEmissive) {
        this.blockRenderer = new GeoBlockRenderer<>(name, isTransparent, isEmissive);
        this.itemRenderer = new GeoItemRenderer<>(name, GeoType.BLOCK, isTransparent, isEmissive);
    }

    public AbstractGeoBlockRenderer(String name) {
        this.blockRenderer = new GeoBlockRenderer<>(name);
        this.itemRenderer = new GeoItemRenderer<>(name, GeoType.BLOCK);
    }

    public GeoBlockRenderer.Renderer<B> getBlockRenderer() {
        return blockRenderer.getRenderer();
    }

    public GeoItemRenderer.Renderer<I> getItemRenderer() {
        return itemRenderer.getRenderer();
    }
}
