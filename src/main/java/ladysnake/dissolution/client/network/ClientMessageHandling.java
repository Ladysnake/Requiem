package ladysnake.dissolution.client.network;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.client.DissolutionFx;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.UUID;
import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

public class ClientMessageHandling {
    public static void init() {
        register(REMNANT_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            boolean remnant = buf.readBoolean();
            boolean incorporeal = buf.readBoolean();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            if (player != null) {
                if (remnant) {
                    ((DissolutionPlayer)player).setRemnant(true);
                    ((DissolutionPlayer) player).getRemnantState().setSoul(incorporeal);
                } else {
                    ((DissolutionPlayer)player).setRemnant(false);
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
                    ((DissolutionPlayer)player).getPossessionComponent().startPossessing((MobEntity) entity);
                    client.gameRenderer.onCameraEntitySet(entity);
                } else {
                    ((DissolutionPlayer)player).getPossessionComponent().stopPossessing();
                    client.gameRenderer.onCameraEntitySet(player);
                }
            }
        }));
        register(ETHEREAL_ANIMATION, ((context, buf) -> DissolutionFx.INSTANCE.beginEtherealAnimation()));
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ClientSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
