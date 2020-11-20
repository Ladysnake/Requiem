package ladysnake.pandemonium.common.network;

import ladysnake.pandemonium.common.PlayerSplitter;
import ladysnake.pandemonium.common.remnant.PlayerBodyTracker;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import static ladysnake.requiem.common.network.RequiemNetworking.ETHEREAL_FRACTURE;

public class ServerMessageHandling {
    public static void init() {
        ServerSidePacketRegistry.INSTANCE.register(ETHEREAL_FRACTURE, (context, buf) -> context.getTaskQueue().execute(() -> {
            PlayerEntity player = context.getPlayer();
            RemnantComponent remnantState = RemnantComponent.get(player);

            if (remnantState.getRemnantType().isDemon()) {
                PossessionComponent possessionComponent = PossessionComponent.get(player);
                if (!remnantState.isSoul()) {
                    PlayerSplitter.split((ServerPlayerEntity) player);
                } else if (canStopPossession(player)) {
                    // TODO make a gamerule to keep the inventory when leaving a mob
                    possessionComponent.stopPossessing();
                } else {
                    return;
                }
                PandemoniumNetworking.sendEtherealAnimationMessage((ServerPlayerEntity) player);
            }
        }));
    }

    private static boolean canStopPossession(PlayerEntity player) {
        MobEntity possessedEntity = PossessionComponent.get(player).getPossessedEntity();
        return possessedEntity != null && (PlayerBodyTracker.get(player).getAnchor() != null
            || RequiemEntityTypeTags.IMMOVABLE.contains(possessedEntity.getType())
        );
    }

}
