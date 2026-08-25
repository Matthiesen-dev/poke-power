package dev.matthiesen.poke_power.common.item;

import dev.matthiesen.poke_power.common.registry.BlockRegistry;
import net.minecraft.world.item.BlockItem;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.Consumer;

public final class PowerBlockItem extends BlockItem implements GeoItem {
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.power_block.idle");
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();

    public RawAnimation getIdleAnimation() {
        return IDLE_ANIMATION;
    }

    public PowerBlockItem() {
        super(BlockRegistry.POWER_BLOCK.get(), new Properties());
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.renderProviderHolder.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state ->
                state.setAndContinue(getIdleAnimation())
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
