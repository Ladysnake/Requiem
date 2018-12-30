package ladysnake.dissolution.mixin.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;DDDFF)V"
            ),
            method = "render"
    )
    public void preRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, CallbackInfo info) {
        RemnantHandler renderedPlayerRemnantHandler = ((DissolutionPlayer) renderedPlayer).getRemnantHandler();
        if (renderedPlayerRemnantHandler != null && renderedPlayerRemnantHandler.isIncorporeal()) {
            Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            boolean isObserverRemnant = cameraEntity instanceof DissolutionPlayer && ((DissolutionPlayer) cameraEntity).getRemnantHandler() != null;
            float alpha = isObserverRemnant ? 0.8f : 0.05f;
            GlStateManager.color4f(0.9f, 0.9f, 1.0f, alpha); // Tints souls blue and transparent
        }
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    shift = AFTER,
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;DDDFF)V"
            ),
            method = "render"
    )
    public void postRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, CallbackInfo info) {
        GlStateManager.color4f(1f, 1f, 1f, 1f);
    }
}
