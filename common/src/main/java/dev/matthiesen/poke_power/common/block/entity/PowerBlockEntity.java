package dev.matthiesen.poke_power.common.block.entity;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.OriginalTrainerType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.config.PokePowerConfig;
import dev.matthiesen.poke_power.common.energy.PokeEnergyGenerator;
import dev.matthiesen.poke_power.common.network.SyncGeneratorPayload;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PowerBlockEntity extends AbstractEnergyBlockEntity implements GeoBlockEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
            .thenLoop("animation.power_block.idle");
    // Plays activate once, then loops active — setAndContinue won't restart mid-play
    private static final RawAnimation ACTIVATE_ANIM = RawAnimation.begin()
            .thenPlay("animation.power_block.activate")
            .thenLoop("animation.power_block.active");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PokeEnergyGenerator generator;

    public PowerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.POWER_BLOCK_BE.get(), blockPos, blockState);
        generator = new PokeEnergyGenerator(getConfigCapacity(), getConfigMaxExtract());
    }

    private long getConfigCapacity() {
        return PokePowerConfig.SERVER_CONFIG.blocks_powerBlock_capacity.get();
    }

    private long getConfigMaxExtract() {
        return PokePowerConfig.SERVER_CONFIG.blocks_powerMax_extract.get();
    }

    private int tickCounter = 0;
    private static final int TICKS_PER_CONFIG_CHECK = 20 * 60; // Check every 60 seconds

    private void verifyEnergyStorageFromConfig() {
        if (tickCounter >= TICKS_PER_CONFIG_CHECK) {
            long configCapacity = getConfigCapacity();
            long configMaxExtract = getConfigMaxExtract();
            if (generator.getCapacity() != configCapacity || generator.getMaxExtract() != configMaxExtract) {
                generator.setCapacity(configCapacity);
                generator.setMaxExtract(configMaxExtract);
            }
            tickCounter = 0;
            return;
        }
        tickCounter++;
    }

    private boolean isActive = false;
    private List<StoredPokemon> storedPokemon = new ArrayList<>();
    private ItemStack chargingItem = ItemStack.EMPTY;

    public boolean insertPokemon(Pokemon pokemon, UUID ownerUuid) {
        if (storedPokemon.size() >= 6) return false;
        if (!pokemon.getPrimaryType().equals(ElementalTypes.ELECTRIC)) return false;
        storedPokemon.add(new StoredPokemon(pokemon, ownerUuid));
        return true;
    }

    public long getActiveGenerationValue() {
        if (!isActive) return 0;
        long totalGeneration = 0;
        for (StoredPokemon stored : storedPokemon) {
            totalGeneration += (long) stored.pokemon().getLevel() * PokePowerConfig.SERVER_CONFIG.powerPerPokeLevel.getAsInt();
        }
        return totalGeneration;
    }

    public List<Pokemon> getStoredPokemon() {
        List<Pokemon> pokemon = new ArrayList<>(storedPokemon.size());
        for (StoredPokemon stored : storedPokemon) {
            pokemon.add(stored.pokemon());
        }
        return Collections.unmodifiableList(pokemon);
    }

    public ItemStack getChargingItem() {
        return chargingItem;
    }

    public void setChargingItem(ItemStack stack) {
        chargingItem = stack;
        setChanged();
    }

    public ItemStack removeChargingItem() {
        ItemStack removed = chargingItem;
        chargingItem = ItemStack.EMPTY;
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    public boolean returnPokemonToOwner(int index) {
        if (level == null || index < 0 || index >= storedPokemon.size()) return false;

        StoredPokemon stored = storedPokemon.get(index);
        if (stored.ownerUuid() == null) return false;

        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(stored.ownerUuid(), level.registryAccess());
        if (!party.add(stored.pokemon())) return false;

        storedPokemon.remove(index);
        return true;
    }

    public void returnStoredPokemonToOwners() {
        if (level == null || level.isClientSide) return;

        for (int i = storedPokemon.size() - 1; i >= 0; i--) {
            returnPokemonToOwner(i);
        }
    }

    /** Builds the sync payload sent to the client when the generator menu is opened or updated. */
    public SyncGeneratorPayload buildSyncPacket(ServerPlayer player, int containerId) {
        List<ItemStack> genItems = new ArrayList<>();
        for (StoredPokemon stored : storedPokemon) {
            genItems.add(new PokeUtil(stored.pokemon()).toItem());
        }
        while (genItems.size() < 6) genItems.add(ItemStack.EMPTY);

        List<ItemStack> partyItems = new ArrayList<>();
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (int i = 0; i < 6; i++) {
            Pokemon pokemon = party.get(i);
            partyItems.add(pokemon != null ? new PokeUtil(pokemon).toItem() : ItemStack.EMPTY);
        }
        return new SyncGeneratorPayload(containerId, getBlockPos(), genItems, partyItems);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state ->
                state.setAndContinue(this.isActive ? ACTIVATE_ANIM : IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public AbstractCommonEnergyStorage getEnergyStorage() {
        return generator;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        CompoundTag nbt = compoundTag.getCompound("poke_power");
        this.isActive = nbt.getBoolean("isActive");
        this.chargingItem = nbt.contains("chargingItem", Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(provider, nbt.getCompound("chargingItem"))
                : ItemStack.EMPTY;
        CompoundTag pokemonList = nbt.getCompound("storedPokemon");
        List<StoredPokemon> loadedStoredPokemon = new ArrayList<>();
        List<String> keys = new ArrayList<>(pokemonList.getAllKeys());
        keys.sort(Comparator.comparingInt(PowerBlockEntity::storedPokemonIndex));
        for (String key : keys) {
            CompoundTag entryTag = pokemonList.getCompound(key);
            CompoundTag pokemonTag = entryTag.contains("pokemon") ? entryTag.getCompound("pokemon") : entryTag;
            Pokemon pokemon = new Pokemon();
            pokemon.loadFromNBT((RegistryAccess) provider, pokemonTag);
            UUID ownerUuid = entryTag.hasUUID("owner") ? entryTag.getUUID("owner") : getOwnerUuid(pokemon);
            loadedStoredPokemon.add(new StoredPokemon(pokemon, ownerUuid));
        }
        this.storedPokemon = loadedStoredPokemon;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("isActive", this.isActive);
        if (!chargingItem.isEmpty()) {
            nbt.put("chargingItem", chargingItem.save(provider));
        }
        CompoundTag pokemonList = new CompoundTag();
        for (int i = 0; i < storedPokemon.size(); i++) {
            StoredPokemon stored = storedPokemon.get(i);
            CompoundTag entryTag = new CompoundTag();
            CompoundTag pokemonTag = stored.pokemon().saveToNBT((RegistryAccess) provider, new CompoundTag());
            entryTag.put("pokemon", pokemonTag);
            if (stored.ownerUuid() != null) {
                entryTag.putUUID("owner", stored.ownerUuid());
            }
            pokemonList.put("pokemon_" + i, entryTag);
        }
        nbt.put("storedPokemon", pokemonList);
        compoundTag.put("poke_power", nbt);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (!(blockEntity instanceof PowerBlockEntity powerBlock)) return;
        if (level.isClientSide) return;
        powerBlock.verifyEnergyStorageFromConfig();

        // Auto-drive isActive from whether any pokemon are stored
        boolean shouldBeActive = !powerBlock.storedPokemon.isEmpty();
        if (shouldBeActive != powerBlock.isActive) {
            powerBlock.isActive = shouldBeActive;
            // Sync to client so the animation controller sees the updated state
            level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
        }

        if (powerBlock.isActive && powerBlock.getActiveGenerationValue() > 0) {
            powerBlock.generator.generate(powerBlock.getActiveGenerationValue());
        }
        powerBlock.chargeItemInSlot();
        powerBlock.generator.distributeEnergy(level, blockPos);
        setChanged(level, blockPos, blockState);
    }

    private void chargeItemInSlot() {
        if (chargingItem.isEmpty()) return;
        long available = generator.getEnergy();
        if (available <= 0) return;

        long maxTransfer = Math.min(available, generator.getMaxExtract());
        if (maxTransfer <= 0) return;

        long accepted = PokePowerCommon.POWER_TOOLS.chargeItem(chargingItem, maxTransfer);
        if (accepted <= 0) return;

        generator.setEnergy(available - accepted);
        setChanged();
    }

    private static int storedPokemonIndex(String key) {
        if (key.startsWith("pokemon_")) {
            try {
                return Integer.parseInt(key.substring("pokemon_".length()));
            } catch (NumberFormatException ignored) {
            }
        }
        return Integer.MAX_VALUE;
    }

    private static UUID getOwnerUuid(Pokemon pokemon) {
        if (pokemon.getOwnerUUID() == null) {
            return getOTUuid(pokemon);
        }
        try {
            return pokemon.getOwnerUUID();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static UUID getOTUuid(Pokemon pokemon) {
        if (pokemon.getOriginalTrainerType() != OriginalTrainerType.PLAYER || pokemon.getOriginalTrainer() == null) return null;
        try {
            return UUID.fromString(pokemon.getOriginalTrainer());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private record StoredPokemon(Pokemon pokemon, UUID ownerUuid) {
    }
}
