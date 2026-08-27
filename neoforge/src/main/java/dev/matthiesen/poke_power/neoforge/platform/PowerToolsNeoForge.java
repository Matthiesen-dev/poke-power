package dev.matthiesen.poke_power.neoforge.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.matthiesen_core.neoforge.api.energy.NeoForgeEnergyHelpers;
import dev.matthiesen.matthiesen_core.neoforge.api.energy.NeoForgeEnergyWrapper;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

public final class PowerToolsNeoForge implements PowerTools {
    private static final NeoForgeEnergyHelpers HELPERS = new NeoForgeEnergyHelpers();

    @Override
    public void registerBlockEntityEnergyCapability(Supplier<BlockEntityType<?>> supplier) {
        NeoForge.EVENT_BUS.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                supplier.get(),
                (blockEntity, side) -> {
                    if (blockEntity instanceof AbstractEnergyBlockEntity energyBlock && isPokePowerBlockEntity(blockEntity)) {
                        return new NeoForgeEnergyWrapper(energyBlock.getEnergyStorage());
                    }
                    return null;
                }
        ));
    }

    @Override
    public void registerItemEnergyCapability(ItemLike... itemLikes) {
        // Opt out
    }

    @Override
    public void distributeEnergy(AbstractCommonEnergyStorage storage, Level level, BlockPos pos) {
        HELPERS.distributeEnergy(storage, level, pos);
    }

    @Override
    public boolean supportsEnergyTransfer(Level level, BlockPos pos, Direction direction) {
        return HELPERS.supportsEnergyTransfer(level, pos, direction);
    }

    @Override
    public long pushEnergyTo(long maxAmount, Level level, BlockPos targetPos, Direction fromSide) {
        return HELPERS.pushEnergyTo(maxAmount, level, targetPos, fromSide);
    }

    @Override
    public boolean canChargeItem(ItemStack stack) {
        return HELPERS.canChargeItem(stack);
    }

    @Override
    public long chargeItem(ItemStack stack, long maxAmount) {
        return HELPERS.chargeItem(stack, maxAmount);
    }
}
