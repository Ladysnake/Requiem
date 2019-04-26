package ladysnake.pandemonium.client;

import ladysnake.pandemonium.api.PandemoniumWorld;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.requiem.client.RequiemFx;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.BiConsumer;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.*;

public class ClientMessageHandling {
    private static final float[] ETHEREAL_DAMAGE_COLOR = {0.5f, 0.0f, 0.0f};

    public static void init() {
        register(ANCHOR_DAMAGE, ((context, buf) -> {
            boolean dead = buf.readBoolean();
            RequiemFx.INSTANCE.playEtherealPulseAnimation(dead ? 4 : 1, ETHEREAL_DAMAGE_COLOR[0], ETHEREAL_DAMAGE_COLOR[1], ETHEREAL_DAMAGE_COLOR[2]);
        }));
        register(ANCHOR_SYNC, ((context, buf) -> {
            int id = buf.readInt();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            ((ClientAnchorManager)((PandemoniumWorld)context.getPlayer().world).getAnchorManager())
                    .getOrCreate(id).setPosition(x, y, z);
        }));
        register(ANCHOR_REMOVE, ((context, buf) -> {
            int id = buf.readInt();
            FractureAnchorManager manager = ((PandemoniumWorld)context.getPlayer().world).getAnchorManager();
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
