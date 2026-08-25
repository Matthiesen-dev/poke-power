package dev.matthiesen.poke_power.common.network;

import dev.matthiesen.matthiesen_core.common.core.network.PacketContext;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SyncGeneratorPayload(
        int containerId,
        BlockPos blockPos,
        List<ItemStack> genItems,
        List<ItemStack> partyItems
) implements CustomPacketPayload {

    public static final Type<SyncGeneratorPayload> TYPE =
            new Type<>(new ResourceLocation(PokePowerCommon.MOD_ID, "sync_generator"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGeneratorPayload> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.containerId());
                        buf.writeBlockPos(p.blockPos());
                        buf.writeCollection(p.genItems(), (b, item) -> ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, item));
                        buf.writeCollection(p.partyItems(), (b, item) -> ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, item));
                    },
                    buf -> {
                        int containerId = buf.readVarInt();
                        BlockPos pos = buf.readBlockPos();
                        List<ItemStack> gen = buf.readList(b -> ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) b));
                        List<ItemStack> party = buf.readList(b -> ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) b));
                        return new SyncGeneratorPayload(containerId, pos, gen, party);
                    }
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client-side handler: update the open PowerGeneratorMenu with the synced data. */
    public static void handleClient(SyncGeneratorPayload payload, PacketContext context) {
        context.enqueue(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
                    && mc.player.containerMenu instanceof PowerGeneratorMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.syncFromServer(payload.blockPos(), payload.genItems(), payload.partyItems());
            }
        });
    }
}
