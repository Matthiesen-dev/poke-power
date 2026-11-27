package dev.matthiesen.poke_power.common.defs;

import com.cobblemon.mod.common.pokemon.OriginalTrainerType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record StoredPokemon(Pokemon pokemon, UUID ownerUuid) {
    private static final String NBT_KEY_POKEMON = "pokemon";
    private static final String NBT_KEY_POKEMON_PREFIX = NBT_KEY_POKEMON + "_";
    private static final String NBT_KEY_POKEMON_OWNER = "owner";
    private static final String NBT_KEY_STORED_POKEMON = "storedPokemon";

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag pokemonTag = pokemon().saveToNBT((RegistryAccess) provider, new CompoundTag());
        tag.put(NBT_KEY_POKEMON, pokemonTag);
        if (ownerUuid() != null) {
            tag.putUUID(NBT_KEY_POKEMON_OWNER, ownerUuid());
        }
        return tag;
    }

    public static StoredPokemon fromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag pokemonTag = tag.contains(NBT_KEY_POKEMON) ? tag.getCompound(NBT_KEY_POKEMON) : tag;
        Pokemon pokemon = new Pokemon();
        pokemon.loadFromNBT((RegistryAccess) provider, pokemonTag);
        UUID ownerUuid = tag.hasUUID(NBT_KEY_POKEMON_OWNER) ? tag.getUUID(NBT_KEY_POKEMON_OWNER) : getOwnerUuid(pokemon);
        return new StoredPokemon(pokemon, ownerUuid);
    }

    public static CompoundTag listToNBT(List<StoredPokemon> storedPokemon, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < storedPokemon.size(); i++) {
            StoredPokemon stored = storedPokemon.get(i);
            CompoundTag entryTag = stored.toNBT(provider);
            tag.put(NBT_KEY_POKEMON_PREFIX + i, entryTag);
        }
        return tag;
    }

    public static void saveToNBT(List<StoredPokemon> storedPokemon, CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag storedList = listToNBT(storedPokemon, provider);
        tag.put(NBT_KEY_STORED_POKEMON, storedList);
    }

    public static List<StoredPokemon> listFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag pokemonList = tag.getCompound(NBT_KEY_STORED_POKEMON);

        List<StoredPokemon> storedPokemon = new ArrayList<>();
        List<String> keys = new ArrayList<>(pokemonList.getAllKeys());

        keys.sort(Comparator.comparingInt(StoredPokemon::storedPokemonIndex));

        for (String key : keys) {
            if (key.startsWith(NBT_KEY_POKEMON_PREFIX)) {
                CompoundTag entryTag = pokemonList.getCompound(key);
                StoredPokemon stored = fromNBT(entryTag, provider);
                storedPokemon.add(stored);
            }
        }

        return storedPokemon;
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

    private static int storedPokemonIndex(String key) {
        if (key.startsWith(NBT_KEY_POKEMON_PREFIX)) {
            try {
                return Integer.parseInt(key.substring(NBT_KEY_POKEMON_PREFIX.length()));
            } catch (NumberFormatException ignored) {
            }
        }
        return Integer.MAX_VALUE;
    }
}
