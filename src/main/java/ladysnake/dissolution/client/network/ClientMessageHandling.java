package ladysnake.dissolution.client.network;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.common.impl.remnant.DefaultRemnantHandler;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.UUID;
import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.network.DissolutionNetworking.POSSESSION_SYNC;
import static ladysnake.dissolution.common.network.DissolutionNetworking.REMNANT_SYNC;

public class ClientMessageHandling {
    public static void init() {
        register(REMNANT_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            boolean remnant = buf.readBoolean();
            boolean incorporeal = buf.readBoolean();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            if (player != null) {
                if (remnant) {
                    DefaultRemnantHandler.getOrMakeRemnant(player).setSoul(incorporeal);
                } else {
                    ((DissolutionPlayer)player).setRemnantHandler(null);
                }
            }
        });
        register(POSSESSION_SYNC, ((context, buf) -> {
            UUID playerUuid = buf.readUuid();
            int possessedId = buf.readInt();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            MinecraftClient client = MinecraftClient.getInstance();
            if (player != null) {
                Entity entity = player.world.getEntityById(possessedId);
                if (entity instanceof MobEntity) {
                    ((DissolutionPlayer)player).startPossessing((MobEntity) entity);
                    client.worldRenderer.onSetCameraEntity(entity);
                } else {
                    ((DissolutionPlayer)player).stopPossessing();
                    client.worldRenderer.onSetCameraEntity(player);
                }
            }
        }));
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        CustomPayloadPacketRegistry.CLIENT.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
