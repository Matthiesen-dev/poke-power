package dev.matthiesen.poke_power.common.network;

import dev.matthiesen.matthiesen_core.common.core.network.PacketContext;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RemovePokemonPayload(BlockPos pos, int genSlotIndex) implements CustomPacketPayload {

    public static final Type<RemovePokemonPayload> TYPE =
            new Type<>(new ResourceLocation(PokePowerCommon.MOD_ID, "remove_pokemon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePokemonPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeBlockPos(p.pos());
                        buf.writeVarInt(p.genSlotIndex());
                    },
                    buf -> new RemovePokemonPayload(buf.readBlockPos(), buf.readVarInt())
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemovePokemonPayload payload, PacketContext context) {
        context.enqueue(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!(player.level().getBlockEntity(payload.pos()) instanceof PowerBlockEntity entity)) return;

            List<Pokemon> stored = entity.getStoredPokemon();
            if (payload.genSlotIndex() < 0 || payload.genSlotIndex() >= stored.size()) return;

            if (entity.returnPokemonToOwner(payload.genSlotIndex())) {
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
