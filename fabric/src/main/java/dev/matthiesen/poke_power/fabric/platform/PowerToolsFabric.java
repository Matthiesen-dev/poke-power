package dev.matthiesen.poke_power.fabric.platform;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.matthiesen_core.fabric.api.energy.FabricEnergyHelpers;
import dev.matthiesen.matthiesen_core.fabric.api.energy.FabricEnergyWrapper;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Supplier;

public final class PowerToolsFabric implements PowerTools {
    private static final FabricEnergyHelpers HELPERS = new FabricEnergyHelpers();

    @Override
    public void registerBlockEntityEnergyCapability(Supplier<BlockEntityType<?>> supplier) {
        EnergyStorage.SIDED.registerForBlockEntities((blockEntity, side) -> {
            if (blockEntity instanceof AbstractEnergyBlockEntity energyBlock && isPokePowerBlockEntity(blockEntity)) {
                return new FabricEnergyWrapper(energyBlock.getEnergyStorage());
            }
            return null;
        }, supplier.get());
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
