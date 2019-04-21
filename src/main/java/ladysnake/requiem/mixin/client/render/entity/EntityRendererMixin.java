package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "postRender", at = @At("HEAD"), cancellable = true)
    private void postRender(Entity rendered, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (rendered instanceof PlayerEntity) {
            if (((RequiemPlayer)rendered).getRemnantState().isSoul()) {
                ci.cancel();
            }
        }
    }
}
