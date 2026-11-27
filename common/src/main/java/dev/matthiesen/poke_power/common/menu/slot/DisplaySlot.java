package dev.matthiesen.poke_power.common.menu.slot;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A read-only slot used for displaying Pokémon icons.
 * All item movement is blocked; clicks are handled by the screen via C2S packets.
 */
public final class DisplaySlot extends Slot {
    public DisplaySlot(SimpleContainer container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override public boolean mayPlace(ItemStack stack)  { return false; }
    @Override public boolean mayPickup(Player player)   { return false; }
}
