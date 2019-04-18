package ladysnake.requiem.common.network;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.BiConsumer;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ServerMessageHandling {

    public static void init() {
        register(LEFT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.ATTACK);
            }
        });
        register(RIGHT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.INTERACT);
            }
        });
        register(ETHEREAL_FRACTURE, (context, buf) -> ((RequiemPlayer) context.getPlayer()).getRemnantState().fracture());

        ServerSidePacketRegistry.INSTANCE.register(POSSESSION_REQUEST, (context, buf) -> {
            int requestedId = buf.readInt();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                Entity entity = player.world.getEntityById(requestedId);
                if (entity instanceof MobEntity && entity.distanceTo(player) < 20) {
                    ((RequiemPlayer) player).getPossessionComponent().startPossessing((MobEntity) entity);
                }
            });
        });
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ServerSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
