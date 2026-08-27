package dev.matthiesen.poke_power.common.menu;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.registry.MenuRegistry;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PowerGeneratorMenu extends AbstractContainerMenu {

    // Slot index ranges exposed to the screen for click handling
    public static final int GEN_SLOT_START = 0;
    public static final int GEN_SLOT_COUNT = 6;
    public static final int PARTY_SLOT_START = 6;
    public static final int PARTY_SLOT_COUNT = 6;
    public static final int CHARGING_SLOT_START = 12;
    public static final int CHARGING_SLOT_COUNT = 1;

    private static final int PLAYER_SLOT_START = CHARGING_SLOT_START + CHARGING_SLOT_COUNT;
    private static final int PLAYER_SLOT_COUNT = 36;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + PLAYER_SLOT_COUNT;

    // Pixel positions used by both menu and screen
    public static final int GEN_SLOT_X0 = 9;
    public static final int GEN_SLOT_Y = 23;
    public static final int PARTY_SLOT_X0 = 9;
    public static final int PARTY_SLOT_Y = 57;

    public static final int PLAYER_INV_X0 = 8;
    public static final int PLAYER_INV_Y = 96;
    public static final int HOTBAR_Y = 154;

    // Pixel positions for the charging container slot
    public static final int CHARGING_SLOT_X = 128;
    public static final int CHARGING_SLOT_Y = 40;

    /**
     * ContainerData indices for energy syncing.
     * ContainerData values are synced as 16-bit shorts, so values above 32767 get
     * corrupted. Energy and capacity can reach 120,000+, so each is split across
     * two slots: low 16 bits and high 16 bits.
     */
    private static final int DATA_ENERGY_LO   = 0;
    private static final int DATA_ENERGY_HI   = 1;
    private static final int DATA_CAPACITY_LO = 2;
    private static final int DATA_CAPACITY_HI = 3;
    private static final int DATA_COUNT       = 4;

    private final SimpleContainer genContainer   = new SimpleContainer(GEN_SLOT_COUNT);
    private final SimpleContainer partyContainer = new SimpleContainer(PARTY_SLOT_COUNT);
    @Nullable private PowerBlockEntity blockEntity;
    private ItemStack clientChargingItem = ItemStack.EMPTY;
    private final Container chargingContainer = new Container() {
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
            return blockEntity != null ? blockEntity.getChargingItem() : clientChargingItem;
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
            if (blockEntity != null) {
                blockEntity.setChargingItem(stack);
            } else {
                clientChargingItem = stack;
            }
            setChanged();
        }

        @Override
        public void setChanged() {
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
    };

    @Nullable private BlockPos blockPos;
    @Nullable private Level level;

    /**
     * ContainerData that reads live from the block entity on the server side
     * and is automatically broadcast to the client every tick via broadcastChanges().
     * On the client, MC calls set() to update the cached values below.
     */
    private long cachedEnergy   = 0;
    private long cachedCapacity = 0;

    private final ContainerData energyData = new ContainerData() {
        @Override
        public int get(int index) {
            // Server: read live from entity so broadcastChanges() detects changes each tick.
            // Client: return cached values written by set().
            long energy   = cachedEnergy;
            long capacity = cachedCapacity;
            if (level != null && !level.isClientSide() && blockPos != null) {
                if (level.getBlockEntity(blockPos) instanceof PowerBlockEntity entity) {
                    energy   = entity.getEnergyStorage().getEnergy();
                    capacity = entity.getEnergyStorage().getCapacity();
                }
            }
            return switch (index) {
                case DATA_ENERGY_LO   -> (int) (energy   & 0xFFFFL);
                case DATA_ENERGY_HI   -> (int) (energy   >> 16);
                case DATA_CAPACITY_LO -> (int) (capacity & 0xFFFFL);
                case DATA_CAPACITY_HI -> (int) (capacity >> 16);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Client-side: MC calls this when the server sends an update.
            // Each value arrives as a signed short; mask to 16 bits before reassembling.
            int bits = value & 0xFFFF;
            switch (index) {
                case DATA_ENERGY_LO   -> cachedEnergy   = (cachedEnergy   & 0xFFFF0000L) | bits;
                case DATA_ENERGY_HI   -> cachedEnergy   = (cachedEnergy   & 0x0000FFFFL) | ((long) bits << 16);
                case DATA_CAPACITY_LO -> cachedCapacity = (cachedCapacity & 0xFFFF0000L) | bits;
                case DATA_CAPACITY_HI -> cachedCapacity = (cachedCapacity & 0x0000FFFFL) | ((long) bits << 16);
            }
        }

        @Override
        public int getCount() { return DATA_COUNT; }
    };

    /** Returns the client-side cached energy (valid on both sides after sync). */
    public long getSyncedEnergy()   { return cachedEnergy; }
    /** Returns the client-side cached capacity (valid on both sides after sync). */
    public long getSyncedCapacity() { return cachedCapacity; }

    /** Client-side constructor — registered in {@link MenuRegistry}. */
    public PowerGeneratorMenu(int containerId, Inventory inv) {
        super(MenuRegistry.POWER_GENERATOR_MENU.get(), containerId);
        addGenSlots();
        addPartySlots();
        addChargingSlot();
        addPlayerInventorySlots(inv);
        addDataSlots(energyData);
    }

    /** Server-side constructor — used when opening the menu for a player. */
    public PowerGeneratorMenu(int containerId, Inventory inv, @Nullable BlockPos pos, PowerBlockEntity entity) {
        super(MenuRegistry.POWER_GENERATOR_MENU.get(), containerId);
        this.blockPos = pos;
        this.level = entity.getLevel();
        this.blockEntity = entity;
        addGenSlots();
        addPartySlots();
        addChargingSlot();
        addPlayerInventorySlots(inv);
        addDataSlots(energyData);

        // Populate generator slots from the entity
        List<Pokemon> stored = entity.getStoredPokemon();
        for (int i = 0; i < stored.size(); i++) {
            genContainer.setItem(i, new PokeUtil(stored.get(i)).toItem());
        }

        // Populate party slots from the player's party
        if (inv.player instanceof ServerPlayer serverPlayer) {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
            for (int i = 0; i < GEN_SLOT_COUNT; i++) {
                Pokemon pokemon = party.get(i);
                if (pokemon != null) {
                    partyContainer.setItem(i, new PokeUtil(pokemon).toItem());
                }
            }
        }
    }

    /**
     * Called from the S2C {@link dev.matthiesen.poke_power.common.network.SyncGeneratorPayload} handler
     * to update the client-side Pokémon slot displays after insert/remove.
     */
    public void syncFromServer(BlockPos pos, List<ItemStack> genItems, List<ItemStack> partyItems) {
        this.blockPos = pos;
        for (int i = 0; i < GEN_SLOT_COUNT; i++) {
            genContainer.setItem(i, i < genItems.size() ? genItems.get(i) : ItemStack.EMPTY);
            partyContainer.setItem(i, i < partyItems.size() ? partyItems.get(i) : ItemStack.EMPTY);
        }
    }

    @Nullable
    public BlockPos getBlockPos() { return blockPos; }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack sourceCopy = sourceStack.copy();

        if (index == CHARGING_SLOT_START) {
            if (!moveItemStackTo(sourceStack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_SLOT_START && index < PLAYER_SLOT_END) {
            if (!moveItemStackTo(sourceStack, CHARGING_SLOT_START, CHARGING_SLOT_START + CHARGING_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return sourceCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockPos == null || level == null) return true;
        return player.distanceToSqr(blockPos.getCenter()) < 64.0
                && level.getBlockEntity(blockPos) instanceof PowerBlockEntity;
    }

    // ── slot helpers ────────────────────────────────────────────────────────

    private void addGenSlots() {
        for (int i = 0; i < GEN_SLOT_COUNT; i++) {
            addSlot(new DisplaySlot(genContainer, i, GEN_SLOT_X0 + i * 18, GEN_SLOT_Y));
        }
    }

    private void addPartySlots() {
        for (int i = 0; i < PARTY_SLOT_COUNT; i++) {
            addSlot(new DisplaySlot(partyContainer, i, PARTY_SLOT_X0 + i * 18, PARTY_SLOT_Y));
        }
    }

    private void addChargingSlot() {
        addSlot(new ChargingSlot(chargingContainer, 0, CHARGING_SLOT_X, CHARGING_SLOT_Y));
    }

    private void addPlayerInventorySlots(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, PLAYER_INV_X0 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, PLAYER_INV_X0 + col * 18, HOTBAR_Y));
        }
    }

    /**
     * A read-only slot used for displaying Pokémon icons.
     * All item movement is blocked; clicks are handled by the screen via C2S packets.
     */
    private static class DisplaySlot extends Slot {
        DisplaySlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override public boolean mayPlace(ItemStack stack)  { return false; }
        @Override public boolean mayPickup(Player player)   { return false; }
    }

    private static class ChargingSlot extends Slot {
        ChargingSlot(Container container, int index, int x, int y) {
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
}
