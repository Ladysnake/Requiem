package ladysnake.dissolution.common.network;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.util.InventoryHelper;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

public class ServerMessageHandling {

    public static void init() {
        register(LEFT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.ATTACK);
            }
        });
        register(RIGHT_CLICK_AIR, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            Possessable possessed = ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.getMobAbilityController().useIndirect(AbilityType.INTERACT);
            }
        });
        register(ETHEREAL_FRACTURE, (context, buf) -> {
            PlayerEntity player = context.getPlayer();
            if (!((DissolutionPlayer)player).isRemnant()) {
                return;
            }
            RemnantState remnantState = ((DissolutionPlayer) player).getRemnantState();
            PossessionComponent possessionComponent = ((DissolutionPlayer) player).getPossessionComponent();
            if (!remnantState.isSoul()) {
                PlayerShellEntity shellEntity = new PlayerShellEntity(player);
                InventoryHelper.transferEquipment(player, shellEntity);
                shellEntity.transferInventory(player.inventory, shellEntity.getInventory(), shellEntity.getInventory().getInvSize());
                player.world.spawnEntity(shellEntity);
                remnantState.setSoul(true);
            } else if (possessionComponent.isPossessing()) {
                possessionComponent.stopPossessing();
            }
        });
        ServerSidePacketRegistry.INSTANCE.register(POSSESSION_REQUEST, (context, buf) -> {
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
        ServerSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> handler.accept(context, packet))
        );
    }
}
