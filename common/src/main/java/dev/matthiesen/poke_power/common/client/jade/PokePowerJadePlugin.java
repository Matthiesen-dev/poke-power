package dev.matthiesen.poke_power.common.client.jade;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.PowerBlock;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class PokePowerJadePlugin implements IWailaPlugin {
    public static final ResourceLocation POKE_POWER_BLOCK = PokePowerCommon.modResource("power_block");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PowerBlockJadeProvider.INSTANCE, PowerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PowerBlockJadeProvider.INSTANCE, PowerBlock.class);
    }
}
