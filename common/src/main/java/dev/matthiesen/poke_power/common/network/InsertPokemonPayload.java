package dev.matthiesen.poke_power.common.network;

import dev.matthiesen.matthiesen_core.common.core.network.PacketContext;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record InsertPokemonPayload(BlockPos pos, int partySlotIndex) implements CustomPacketPayload {

    public static final Type<InsertPokemonPayload> TYPE =
            new Type<>(new ResourceLocation(PokePowerCommon.MOD_ID, "insert_pokemon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InsertPokemonPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBlockPos(p.pos());
                        buf.writeVarInt(p.partySlotIndex());
                    },
                    buf -> new InsertPokemonPayload(buf.readBlockPos(), buf.readVarInt())
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(InsertPokemonPayload payload, PacketContext context) {
        context.enqueue(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!(player.level().getBlockEntity(payload.pos()) instanceof PowerBlockEntity entity)) return;

            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            Pokemon pokemon = party.get(payload.partySlotIndex());
            if (pokemon == null) return;

            if (entity.insertPokemon(pokemon, player.getUUID())) {
                party.remove(pokemon);
                entity.setChanged();
                syncMenuToPlayer(player, entity);
            }
        });
    }

    private static void syncMenuToPlayer(ServerPlayer player, PowerBlockEntity entity) {
        if (player.containerMenu instanceof PowerGeneratorMenu genMenu) {
            PokePowerCommon.INSTANCE.getNetworkingManager()
                    .sendToPlayer(player, entity.buildSyncPacket(player, genMenu.containerId));
        }
    }
}
