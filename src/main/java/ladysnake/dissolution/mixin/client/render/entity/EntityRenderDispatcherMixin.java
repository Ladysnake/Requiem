package ladysnake.dissolution.mixin.client.render.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.client.DissolutionFx;
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
    private Entity dissolution_camerasPossessed;

    @Inject(method = "setRenderPosition", at = @At("HEAD"))
    private void updateCamerasPossessedEntity(double x, double y, double z, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity camera = client.getCameraEntity();
        if (camera instanceof DissolutionPlayer && client.options.field_1850 == 0) {
            dissolution_camerasPossessed = (Entity) ((DissolutionPlayer) camera).getPossessionComponent().getPossessedEntity();
            if (dissolution_camerasPossessed == null) {
                dissolution_camerasPossessed = DissolutionFx.INSTANCE.getAnimationEntity();
            }
        } else {
            dissolution_camerasPossessed = null;
        }
    }

    /**
     * Prevents the camera's possessed entity from rendering
     */
    @Inject(method = "method_3950", at = @At("HEAD"), cancellable = true)
    private void preventPossessedRender(Entity entity, VisibleRegion visibleRegion, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        if (dissolution_camerasPossessed == entity) {
            info.setReturnValue(false);
        }
    }
}
