package ladysnake.dissolution.mixin.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createLeftClickPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToServer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    private ClientConnection clientConnection;

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;method_7350()V"
            )
    )
    public void onShakeFistAtAir(CallbackInfo info) {
        if (((DissolutionPlayer) player).getPossessionManager().isPossessing()) {
            sendToServer(createLeftClickPacket());
        }
    }
}
