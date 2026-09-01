package dev.matthiesen.poke_power.common.client.jade;

import dev.matthiesen.matthiesen_core.common.utility.EnergyUtilities;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.config.PokePowerConfig;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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

    private static final String HAS_POKEMON = "has_pokemon";
    private static final String POKE_PROPERTY_PREFIX = "poke_property_";
    private static final String POKE_PROPERTY_LEVEL_PREFIX = "poke_property_level_";
    private static final String TOTAL_ENERGY_PER_TICK = "total_energy_per_tick";
    private static final long MIN_FORMATTABLE_ENERGY = 1L;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var serverData = blockAccessor.getServerData();
        var registryAccess = blockAccessor.getLevel().registryAccess();

        if (serverData.getBoolean(HAS_POKEMON)) {
            iTooltip.add(Component.literal("Contained Pokemon:").withStyle(ChatFormatting.GRAY));
        }

        int index = 0;
        while (serverData.contains(POKE_PROPERTY_PREFIX + index, Tag.TAG_COMPOUND)) {
            var itemTag = serverData.getCompound(POKE_PROPERTY_PREFIX + index);
            ItemStack item = ItemStack.parseOptional(registryAccess, itemTag);
            if (!item.isEmpty()) {
                IElementHelper elements = IElementHelper.get();
                IElement icon = elements.item(item, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
                iTooltip.add(icon);
                iTooltip.append(Component.literal(" ").append(item.getHoverName().copy().withStyle(ChatFormatting.WHITE)));

                if (serverData.contains(POKE_PROPERTY_LEVEL_PREFIX + index, Tag.TAG_INT)) {
                    int level = serverData.getInt(POKE_PROPERTY_LEVEL_PREFIX + index);
                    var energyPerLevel = getPowerPerTick(level);
                    var formattedEnergy = formatEnergyValueSafe(energyPerLevel);
                    iTooltip.append(Component.translatableEscape("tooltip.poke_power.pokemon.power-gen.jade.pokemon", formattedEnergy).withStyle(ChatFormatting.GRAY));
                }

            }
            index++;
        }

        if (serverData.contains(TOTAL_ENERGY_PER_TICK, Tag.TAG_LONG)) {
            long totalEnergyPerTick = serverData.getLong(TOTAL_ENERGY_PER_TICK);

            ChatFormatting generationColor = totalEnergyPerTick < MIN_FORMATTABLE_ENERGY
                    ? ChatFormatting.RED
                    : ChatFormatting.YELLOW;

            String formattedEnergy = formatEnergyValueSafe(totalEnergyPerTick);
            iTooltip.add(Component.translatable("tooltip.poke_power.pokemon.power-gen.jade").withStyle(ChatFormatting.GRAY).append(
                    Component.translatableEscape("tooltip.poke_power.pokemon.power-gen.value", formattedEnergy).withStyle(generationColor)
            ));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getLevel() instanceof ServerLevel serverLevel)) return;
        if (blockAccessor.getBlockEntity() instanceof PowerBlockEntity powerBlock) {
            boolean hasPokemon = false;

            var pokemonList = powerBlock.getStoredPokemon();
            for (int i = 0; i < pokemonList.size(); i++) {
                ItemStack item = new PokeUtil(pokemonList.get(i)).toItem();
                Tag itemTag = item.save(serverLevel.registryAccess());
                compoundTag.put(POKE_PROPERTY_PREFIX + i, itemTag);
                compoundTag.putInt(POKE_PROPERTY_LEVEL_PREFIX + i, pokemonList.get(i).getLevel());
                hasPokemon = true;
            }

            compoundTag.putBoolean(HAS_POKEMON, hasPokemon);

            compoundTag.putLong(TOTAL_ENERGY_PER_TICK, powerBlock.getActiveGenerationValue());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return PokePowerJadePlugin.POKE_POWER_BLOCK;
    }

    private static String formatEnergyValueSafe(long value) {
        return value < MIN_FORMATTABLE_ENERGY ? "0" : EnergyUtilities.toParsedString(value);
    }

    private int getPowerPerTick(int level) {
        int powerPerPokeLevel = PokePowerConfig.SERVER_CONFIG.blocks_powerBlock_powerPerPokeLevel.getAsInt();
        return level * powerPerPokeLevel;
    }
}
