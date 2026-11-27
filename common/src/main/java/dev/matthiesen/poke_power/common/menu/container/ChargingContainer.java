package dev.matthiesen.poke_power.common.menu.container;

import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A container that wraps a {@link PowerBlockEntity} and exposes its charging slot to the menu.
 * On the server, this container reads/writes directly to the block entity.
 * On the client, this container uses a cached item stack that is updated via C2S packets.
 */
public final class ChargingContainer implements Container {
    private final Supplier<@Nullable PowerBlockEntity> blockEntity;
    private final Supplier<ItemStack> clientChargingItem;
    private final Consumer<@NotNull ItemStack> onItemChanged;

    public ChargingContainer(Supplier<@Nullable PowerBlockEntity> blockEntity, Supplier<ItemStack> clientChargingItem, Consumer<@NotNull ItemStack> onItemChanged) {
        this.blockEntity = blockEntity;
        this.clientChargingItem = clientChargingItem;
        this.onItemChanged = onItemChanged;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        if (index != 0) return ItemStack.EMPTY;
        PowerBlockEntity blockEntity = this.blockEntity.get();
        return blockEntity != null ? blockEntity.getChargingItem() : clientChargingItem.get();
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        if (index != 0 || count <= 0) return ItemStack.EMPTY;
        ItemStack stack = getItem(0);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(count);
        setItem(0, stack.isEmpty() ? ItemStack.EMPTY : stack);
        return removed;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack stack = getItem(0);
        setItem(0, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (index != 0) return;
        PowerBlockEntity blockEntity = this.blockEntity.get();
        if (blockEntity != null) {
            blockEntity.setChargingItem(stack);
        } else {
            onItemChanged.accept(stack);
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        PowerBlockEntity blockEntity = this.blockEntity.get();
        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }
}
