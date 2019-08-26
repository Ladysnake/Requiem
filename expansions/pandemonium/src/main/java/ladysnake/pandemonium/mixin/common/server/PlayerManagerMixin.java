package ladysnake.pandemonium.mixin.common.server;

import ladysnake.pandemonium.api.PandemoniumWorld;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.createAnchorUpdateMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendTo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "sendWorldInfo", at = @At("RETURN"))
    private void sendWorldJoinMessages(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        for (FractureAnchor anchor : ((PandemoniumWorld)world).getAnchorManager().getAnchors()) {
            sendTo(player, createAnchorUpdateMessage(anchor));
        }
    }
}
