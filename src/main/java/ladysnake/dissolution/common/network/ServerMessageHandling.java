package ladysnake.dissolution.common.network;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.possession.Possessable;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.network.DissolutionNetworking.LEFT_CLICK_AIR;
import static ladysnake.dissolution.common.network.DissolutionNetworking.POSSESSION_REQUEST;

public class ServerMessageHandling {

    public static void init() {
        register(LEFT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.ATTACK);
            }
        });
        CustomPayloadPacketRegistry.SERVER.register(POSSESSION_REQUEST, (context, buf) -> {
            int requestedId = buf.readInt();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                Entity entity = player.world.getEntityById(requestedId);
                if (entity instanceof MobEntity && entity.distanceTo(player) < 20) {
                    ((DissolutionPlayer) player).getPossessionComponent().startPossessing((MobEntity) entity);
                }
            });
        });
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        CustomPayloadPacketRegistry.SERVER.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
