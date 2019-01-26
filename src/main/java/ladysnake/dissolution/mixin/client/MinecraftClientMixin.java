package ladysnake.dissolution.mixin.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createLeftClickPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToServer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;method_7350()V"
            )
    )
    private void onShakeFistAtAir(CallbackInfo info) {
        if (((DissolutionPlayer) player).getPossessionComponent().isPossessing()) {
            sendToServer(createLeftClickPacket());
        }
    }
}
