package dev.matthiesen.poke_power.common.menu.slot;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A slot that only accepts items that can be charged by the Power Block.
 */
public final class ChargingSlot extends Slot {
    public ChargingSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return PokePowerCommon.POWER_TOOLS.canChargeItem(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
