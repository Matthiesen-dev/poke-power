package dev.matthiesen.poke_power.common.client.jade;

import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum PowerBlockJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var serverData = blockAccessor.getServerData();
        var registryAccess = blockAccessor.getLevel().registryAccess();
        int index = 0;
        while (serverData.contains("poke_property_" + index, Tag.TAG_COMPOUND)) {
            var itemTag = serverData.getCompound("poke_property_" + index);
            ItemStack item = ItemStack.parseOptional(registryAccess, itemTag);
            if (!item.isEmpty()) {
                IElementHelper elements = IElementHelper.get();
                IElement icon = elements.item(item, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
                iTooltip.add(icon);
                iTooltip.append(item.getHoverName().copy());
            }
            index++;
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getLevel() instanceof ServerLevel serverLevel)) return;
        if (blockAccessor.getBlockEntity() instanceof PowerBlockEntity powerBlock) {
            var pokemonList = powerBlock.getStoredPokemon();
            for (int i = 0; i < pokemonList.size(); i++) {
                ItemStack item = new PokeUtil(pokemonList.get(i)).toItem();
                Tag itemTag = item.save(serverLevel.registryAccess());
                compoundTag.put("poke_property_" + i, itemTag);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return PokePowerJadePlugin.POKE_POWER_BLOCK;
    }
}
