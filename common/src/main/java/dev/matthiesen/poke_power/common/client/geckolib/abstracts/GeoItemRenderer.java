package dev.matthiesen.poke_power.common.client.geckolib.abstracts;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class GeoItemRenderer<T extends Item & GeoAnimatable> {
    private final Renderer<T> renderer;

    public GeoItemRenderer(String name, GeoType type) {
        Model<T> model = new Model<>(name, type);
        this.renderer = new Renderer<>(model, false, false);
    }

    public GeoItemRenderer(String name, GeoType type, boolean isTransparent, boolean isEmissive) {
        Model<T> model = new Model<>(name, type);
        this.renderer = new Renderer<>(model, isTransparent, isEmissive);
    }

    public Renderer<T> getRenderer() {
        return renderer;
    }

    public static class Model<T extends Item & GeoAnimatable> extends GeoModel<T> {
        private final String name;
        private final GeoType type;

        public Model(String name, GeoType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public ResourceLocation getModelResource(T animatable) {
            return PokePowerCommon.modResource("geo/" + type.getName() + "/" + name + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T animatable) {
            return PokePowerCommon.modResource("textures/" + type.getName() + "/" + name + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(T animatable) {
            return PokePowerCommon.modResource("animations/" + type.getName() + "/" + name + ".animation.json");
        }
    }

    public static class Renderer<T extends Item & GeoAnimatable> extends software.bernie.geckolib.renderer.GeoItemRenderer<T> {
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
