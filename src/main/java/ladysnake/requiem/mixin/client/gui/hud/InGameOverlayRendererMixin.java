package ladysnake.requiem.mixin.client.gui.hud;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Unique
    private static boolean wasNoClip;

    @Inject(method = "renderOverlays", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z"))
    private static void obscureVision(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        ClientPlayerEntity player = minecraftClient.player;
        assert player != null;
        if (player.noClip && MovementAlterer.get(player).isNoClipping()) {
            player.noClip = false;
            wasNoClip = true;
        }
    }

    @Inject(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
    private static void reenableNoClip(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        if (wasNoClip) {
            assert minecraftClient.player != null;
            minecraftClient.player.noClip = true;
            wasNoClip = false;
        }
    }
}
