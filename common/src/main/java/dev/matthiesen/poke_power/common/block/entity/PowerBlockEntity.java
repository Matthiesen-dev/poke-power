package dev.matthiesen.poke_power.common.block.entity;

import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.poke_power.common.energy.PokeEnergyGenerator;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.util.PokeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
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
    private static final RawAnimation ACTIVE_ANIM = RawAnimation.begin()
            .thenLoop("animation.power_block.active");
    private static final RawAnimation ACTIVATE_ANIM = RawAnimation.begin()
            .thenPlay("animation.power_block.activate");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PokeEnergyGenerator generator = new PokeEnergyGenerator(48000L, 16000L);

    private boolean isActive = false;
    private List<Pokemon> storedPokemon = new ArrayList<>();

    public void setActive(boolean active) {
        this.isActive = active;
    }

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
            PokeUtil pokeUtil = new PokeUtil(pokemon);
            CompoundTag nbt = pokeUtil.toNBT(this.getLevel().registryAccess());
            int level = nbt.getInt("Level");
            totalGeneration += level * 50L; // Each level contributes 50 energy units
        }
        return totalGeneration;
    }

    public List<Pokemon> getStoredPokemon() {
        return Collections.unmodifiableList(storedPokemon);
    }

    public boolean takePokemon(Pokemon pokemon) {
        return storedPokemon.remove(pokemon);
    }

    public PowerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.POWER_BLOCK_BE.get(), blockPos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state ->
                state.setAndContinue(this.isActive ? ACTIVE_ANIM : IDLE_ANIM))
                .triggerableAnim("activate", ACTIVATE_ANIM));
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
        var pokemonList = nbt.getCompound("storedPokemon");
        List<Pokemon> pokemon = new ArrayList<>();
        for (String key : pokemonList.getAllKeys()) {
            CompoundTag pokemonTag = pokemonList.getCompound(key);
            PokeUtil pokeUtil = PokeUtil.fromNBT(pokemonTag, this.getLevel().registryAccess());
            pokemon.add(pokeUtil.getPokemon());
        }
        this.storedPokemon = Collections.unmodifiableList(pokemon);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("isActive", this.isActive);
        CompoundTag pokemonList = new CompoundTag();
        for (int i = 0; i < storedPokemon.size(); i++) {
            Pokemon pokemon = storedPokemon.get(i);
            PokeUtil pokeUtil = new PokeUtil(pokemon);
            CompoundTag pokemonTag = pokeUtil.toNBT(this.getLevel().registryAccess());
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
        if (powerBlock.isActive && powerBlock.getActiveGenerationValue() > 0) {
            powerBlock.generator.generate(powerBlock.getActiveGenerationValue());
        }
        powerBlock.generator.distributeEnergy(level, blockPos);
        setChanged(level, blockPos, blockState);
    }
}
