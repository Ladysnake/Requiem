package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.client.RequiemFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Nullable
    private Entity requiem_camerasPossessed;

    /**
     * Called once per frame, used to update the entity
     */
    @Inject(method = "setRenderPosition", at = @At("HEAD"))
    private void updateCamerasPossessedEntity(double x, double y, double z, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity camera = client.getCameraEntity();
        if (camera instanceof RequiemPlayer) {
            requiem_camerasPossessed = (Entity) ((RequiemPlayer) camera).getPossessionComponent().getPossessedEntity();
            if (requiem_camerasPossessed == null) {
                requiem_camerasPossessed = RequiemFx.INSTANCE.getAnimationEntity();
            }
        } else {
            requiem_camerasPossessed = null;
        }
    }

    /**
     * Prevents the camera's possessed entity from rendering
     */
    @Inject(method = "method_3950", at = @At("HEAD"), cancellable = true)
    private void preventPossessedRender(Entity entity, VisibleRegion visibleRegion, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        if (requiem_camerasPossessed == entity) {
            info.setReturnValue(false);
        }
    }
}
