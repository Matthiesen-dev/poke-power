package dev.matthiesen.poke_power.common.menu;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.menu.container.ChargingContainer;
import dev.matthiesen.poke_power.common.menu.container.data.ChargingContainerData;
import dev.matthiesen.poke_power.common.menu.slot.ChargingSlot;
import dev.matthiesen.poke_power.common.menu.slot.DisplaySlot;
import dev.matthiesen.poke_power.common.registry.MenuRegistry;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The menu for the Power Block, which allows players to view stored Pokémon, their party, and charge items.
 * This class handles the server-client synchronization of the menu state and manages the slots for display and interaction.
 */
public final class PowerBlockMenu extends AbstractContainerMenu {

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
    public static final int PARTY_SLOT_Y = 61;

    public static final int PLAYER_INV_X0 = 8;
    public static final int PLAYER_INV_Y = 104;
    public static final int HOTBAR_Y = 162;

    // Pixel positions for the charging container slot
    public static final int CHARGING_SLOT_X = 128;
    public static final int CHARGING_SLOT_Y = 40;

    private final SimpleContainer genContainer   = new SimpleContainer(GEN_SLOT_COUNT);
    private final SimpleContainer partyContainer = new SimpleContainer(PARTY_SLOT_COUNT);

    @Nullable private BlockPos blockPos;
    @Nullable private Level level;
    @Nullable private PowerBlockEntity blockEntity;
    private ItemStack clientChargingItem = ItemStack.EMPTY;
    private long cachedEnergy   = 0;
    private long cachedCapacity = 0;
    private long cachedGeneration = 0;

    private final ChargingContainer chargingContainer = new ChargingContainer(
            () -> blockEntity,
            () -> clientChargingItem,
            (stack) -> clientChargingItem = stack
    );

    private final ChargingContainerData energyData = new ChargingContainerData(
        () -> cachedEnergy,
        () -> cachedCapacity,
        () -> cachedGeneration,
        (value) -> cachedEnergy = value,
        (value) -> cachedCapacity = value,
        (value) -> cachedGeneration = value,
        () -> level,
        () -> blockPos
    );

    /** Returns the client-side cached energy (valid on both sides after sync). */
    public long getSyncedEnergy()   { return cachedEnergy; }
    /** Returns the client-side cached capacity (valid on both sides after sync). */
    public long getSyncedCapacity() { return cachedCapacity; }
    /** Returns the client-side cached active generation rate (FE/t). */
    public long getSyncedGeneration() { return cachedGeneration; }

    /** Client-side constructor — registered in {@link MenuRegistry}. */
    public PowerBlockMenu(int containerId, Inventory inv) {
        super(MenuRegistry.POWER_GENERATOR_MENU.get(), containerId);
        setupSlots(inv);
    }

    /** Server-side constructor — used when opening the menu for a player. */
    public PowerBlockMenu(int containerId, Inventory inv, @Nullable BlockPos pos, PowerBlockEntity entity) {
        super(MenuRegistry.POWER_GENERATOR_MENU.get(), containerId);
        this.blockPos = pos;
        this.level = entity.getLevel();
        this.blockEntity = entity;
        setupSlots(inv);

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

    private void setupSlots(Inventory inv) {
        addGenSlots();
        addPartySlots();
        addChargingSlot();
        addPlayerInventorySlots(inv);
        addDataSlots(energyData);
    }

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
}
