package ladysnake.pandemonium.common.network;

import ladysnake.pandemonium.api.PandemoniumWorld;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.pandemonium.common.impl.anchor.AnchorFactories;
import ladysnake.requiem.api.v1.player.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.common.impl.remnant.MutableRemnantState;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import static ladysnake.requiem.common.network.RequiemNetworking.ETHEREAL_FRACTURE;
import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyMessage;

public class ServerMessageHandling {
    public static void init() {
        ServerSidePacketRegistry.INSTANCE.register(ETHEREAL_FRACTURE, (context, buf) -> context.getTaskQueue().execute(() -> {
            PlayerEntity player = context.getPlayer();
            RemnantState remnantState = ((RequiemPlayer)player).getRemnantState();
            if (remnantState instanceof MutableRemnantState) {
                PossessionComponent possessionComponent = ((RequiemPlayer) player).getPossessionComponent();
                FractureAnchorManager anchorManager = ((PandemoniumWorld) player.world).getAnchorManager();
                if (!remnantState.isSoul()) {
                    PlayerShellEntity shellEntity = PlayerShellEntity.fromPlayer(player);
                    player.world.spawnEntity(shellEntity);
                    FractureAnchor anchor = anchorManager.addAnchor(AnchorFactories.fromEntityUuid(shellEntity.getUuid()));
                    anchor.setPosition(shellEntity.x, shellEntity.y, shellEntity.z);
                    remnantState.setSoul(true);
                } else if (possessionComponent.isPossessing()/* && state.getAnchor() != null*/) {
                    possessionComponent.stopPossessing();
                } else {
                    return;
                }
                RequiemNetworking.sendTo((ServerPlayerEntity)player, createEmptyMessage(RequiemNetworking.ETHEREAL_ANIMATION));
            }
        }));
    }
}
