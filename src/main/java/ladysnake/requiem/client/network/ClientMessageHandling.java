package ladysnake.requiem.client.network;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.RequiemWorld;
import ladysnake.requiem.api.v1.remnant.FractureAnchor;
import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
import ladysnake.requiem.client.ClientAnchorManager;
import ladysnake.requiem.client.RequiemFx;
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

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ClientMessageHandling {
    public static void init() {
        register(REMNANT_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            boolean remnant = buf.readBoolean();
            boolean incorporeal = buf.readBoolean();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            if (player != null) {
                if (remnant) {
                    ((RequiemPlayer)player).setRemnant(true);
                    ((RequiemPlayer) player).getRemnantState().setSoul(incorporeal);
                } else {
                    ((RequiemPlayer)player).setRemnant(false);
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
                    ((RequiemPlayer)player).getPossessionComponent().startPossessing((MobEntity) entity);
                    if (client.options.perspective == 0) {
                        client.gameRenderer.onCameraEntitySet(entity);
                    }
                } else {
                    ((RequiemPlayer)player).getPossessionComponent().stopPossessing();
                    client.gameRenderer.onCameraEntitySet(player);
                }
            }
        }));
        register(ETHEREAL_ANIMATION, ((context, buf) -> RequiemFx.INSTANCE.beginEtherealAnimation()));
        register(ANCHOR_DAMAGE, ((context, buf) -> {
            boolean dead = buf.readBoolean();
            RequiemFx.INSTANCE.beginEtherealDamageAnimation(dead);
        }));
        register(ANCHOR_SYNC, ((context, buf) -> {
            int id = buf.readInt();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            ((ClientAnchorManager)((RequiemWorld)context.getPlayer().world).getAnchorManager())
                    .getOrCreate(id).setPosition(x, y, z);
        }));
        register(ANCHOR_REMOVE, ((context, buf) -> {
            int id = buf.readInt();
            FractureAnchorManager manager = ((RequiemWorld)context.getPlayer().world).getAnchorManager();
            FractureAnchor anchor = manager.getAnchor(id);
            if (anchor != null) {
                anchor.invalidate();
            }
        }));
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ClientSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> {
                    handler.accept(context, packet);
                    packet.release();
                })
        );
    }
}
