package dev.matthiesen.poke_power.common.client.geckolib.abstracts;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class GeoBlockRenderer<T extends BlockEntity & GeoAnimatable> {
    private final Renderer<T> renderer;

    public GeoBlockRenderer(String name, boolean isTransparent, boolean isEmissive) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model, isTransparent, isEmissive);
    }

    public GeoBlockRenderer(String name) {
        Model<T> model = new Model<>(name);
        this.renderer = new Renderer<>(model, false, false);
    }

    public Renderer<T> getRenderer() {
        return renderer;
    }

    public static class Model<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {
        private final String name;

        public Model(String name) {
            this.name = name;
        }

        @Override
        public ResourceLocation getModelResource(T animatable) {
            return PokePowerCommon.modResource("geo/block/" + name + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T animatable) {
            return PokePowerCommon.modResource("textures/block/" + name + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(T animatable) {
            return PokePowerCommon.modResource("animations/block/" + name + ".animation.json");
        }
    }

    public static class Renderer<T extends BlockEntity & GeoAnimatable> extends software.bernie.geckolib.renderer.GeoBlockRenderer<T> {
        private final boolean isTransparent;

        public Renderer(GeoModel<T> model, boolean isTransparent, boolean isEmissive) {
            super(model);
            this.isTransparent = isTransparent;
            if (isEmissive) {
                addRenderLayer(new AutoGlowingGeoLayer<>(this));
            }
        }

        @Override
        public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
            if (isTransparent) {
                return RenderType.entityTranslucent(getTextureLocation(animatable));
            }
            return super.getRenderType(animatable, texture, bufferSource, partialTick);
        }
    }
}
