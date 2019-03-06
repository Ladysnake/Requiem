package ladysnake.dissolution.client.network;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionWorld;
import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import ladysnake.dissolution.client.ClientAnchorManager;
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
            // method_18470 == EntityView#getPlayerByUuid
            PlayerEntity player = context.getPlayer().world.method_18470(playerUuid);
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
            // method_18470 == EntityView#getPlayerByUuid
            PlayerEntity player = context.getPlayer().world.method_18470(playerUuid);
            MinecraftClient client = MinecraftClient.getInstance();
            if (player != null) {
                Entity entity = player.world.getEntityById(possessedId);
                if (entity instanceof MobEntity) {
                    ((DissolutionPlayer)player).getPossessionComponent().startPossessing((MobEntity) entity);
                    if (client.options.perspective == 0) {
                        client.gameRenderer.onCameraEntitySet(entity);
                    }
                } else {
                    ((DissolutionPlayer)player).getPossessionComponent().stopPossessing();
                    client.gameRenderer.onCameraEntitySet(player);
                }
            }
        }));
        register(ETHEREAL_ANIMATION, ((context, buf) -> DissolutionFx.INSTANCE.beginEtherealAnimation()));
        register(ANCHOR_SYNC, ((context, buf) -> {
            int id = buf.readInt();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            ((ClientAnchorManager)((DissolutionWorld)context.getPlayer().world).getAnchorManager())
                    .getOrCreate(id).setPosition(x, y, z);
        }));
        register(ANCHOR_REMOVE, ((context, buf) -> {
            int id = buf.readInt();
            FractureAnchorManager manager = ((DissolutionWorld)context.getPlayer().world).getAnchorManager();
            FractureAnchor anchor = manager.getAnchor(id);
            if (anchor != null) {
                manager.removeAnchor(anchor.getUuid());
            }
        }));
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ClientSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
