package dev.matthiesen.poke_power.common.block.entity;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.poke_power.common.energy.PokeEnergyGenerator;
import dev.matthiesen.poke_power.common.network.SyncGeneratorPayload;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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
import java.util.List;

public final class PowerBlockEntity extends AbstractEnergyBlockEntity implements GeoBlockEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin()
            .thenLoop("animation.power_block.idle");
    // Plays activate once, then loops active — setAndContinue won't restart mid-play
    private static final RawAnimation ACTIVATE_ANIM = RawAnimation.begin()
            .thenPlay("animation.power_block.activate")
            .thenLoop("animation.power_block.active");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PokeEnergyGenerator generator = new PokeEnergyGenerator(48000L, 16000L);

    private boolean isActive = false;
    // Mutable list - never wrap in unmodifiableList so insertPokemon/takePokemon work correctly
    private List<Pokemon> storedPokemon = new ArrayList<>();

    public boolean insertPokemon(Pokemon pokemon) {
        if (storedPokemon.size() >= 6) return false;
        if (!pokemon.getPrimaryType().equals(ElementalTypes.ELECTRIC)) return false;
        storedPokemon.add(pokemon);
        return true;
    }

    public long getActiveGenerationValue() {
        if (!isActive) return 0;
        long totalGeneration = 0;
        for (Pokemon pokemon : storedPokemon) {
            totalGeneration += (long) pokemon.getLevel() * 50L;
        }
        return totalGeneration;
    }

    public List<Pokemon> getStoredPokemon() {
        return Collections.unmodifiableList(storedPokemon);
    }

    public boolean takePokemon(Pokemon pokemon) {
        return storedPokemon.remove(pokemon);
    }

    /** Builds the sync payload sent to the client when the generator menu is opened or updated. */
    public SyncGeneratorPayload buildSyncPacket(ServerPlayer player, int containerId) {
        List<ItemStack> genItems = new ArrayList<>();
        for (Pokemon pokemon : storedPokemon) {
            genItems.add(new PokeUtil(pokemon).toItem());
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

    public PowerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.POWER_BLOCK_BE.get(), blockPos, blockState);
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
        CompoundTag pokemonList = nbt.getCompound("storedPokemon");
        // Use a mutable list and use provider (always a RegistryAccess at runtime)
        List<Pokemon> loaded = new ArrayList<>();
        for (String key : pokemonList.getAllKeys()) {
            CompoundTag pokemonTag = pokemonList.getCompound(key);
            Pokemon pokemon = new Pokemon();
            pokemon.loadFromNBT((RegistryAccess) provider, pokemonTag);
            loaded.add(pokemon);
        }
        this.storedPokemon = loaded;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("isActive", this.isActive);
        CompoundTag pokemonList = new CompoundTag();
        for (int i = 0; i < storedPokemon.size(); i++) {
            Pokemon pokemon = storedPokemon.get(i);
            CompoundTag pokemonTag = pokemon.saveToNBT((RegistryAccess) provider, new CompoundTag());
            pokemonList.put("pokemon_" + i, pokemonTag);
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
        powerBlock.generator.distributeEnergy(level, blockPos);
        setChanged(level, blockPos, blockState);
    }
}
