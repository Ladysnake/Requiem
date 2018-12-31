package ladysnake.dissolution.client.network;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.common.impl.DefaultRemnantHandler;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.UUID;
import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.network.DissolutionNetworking.REMNANT_SYNC;

public class ClientMessageHandling {
    public static void init() {
        register(REMNANT_SYNC, (context, packet) -> {
            UUID playerUuid = packet.readUuid();
            boolean remnant = packet.readBoolean();
            boolean incorporeal = packet.readBoolean();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            if (player != null) {
                if (remnant) {
                    DefaultRemnantHandler.getOrMakeRemnant(player).setIncorporeal(incorporeal);
                } else {
                    ((DissolutionPlayer)player).setRemnantHandler(null);
                }
            }
        });
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        CustomPayloadPacketRegistry.CLIENT.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
