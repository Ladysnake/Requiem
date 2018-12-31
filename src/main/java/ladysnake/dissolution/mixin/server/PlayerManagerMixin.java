package ladysnake.dissolution.mixin.server;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createCorporealityPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendTo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity createdPlayer, CallbackInfo info) {
        sendTo(createdPlayer, createCorporealityPacket(createdPlayer));
    }
}
