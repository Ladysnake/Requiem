package ladysnake.requiem.mixin.client.render;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.client.render.RequiemBuilderStorage;
import ladysnake.requiem.client.render.RequiemRenderLayers;
import ladysnake.requiem.client.render.RequiemVertexConsumerProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;
    @Nullable
    private Entity requiem_camerasFocused;

    /**
     * Called once per frame, used to update the entity
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void updateCamerasPossessedEntity(MatrixStack matrices,
                                              float tickDelta,
                                              long limitTime,
                                              boolean renderBlockOutline,
                                              Camera camera,
                                              GameRenderer gameRenderer,
                                              LightmapTextureManager lightmapTextureManager,
                                              Matrix4f matrix4f,
                                              CallbackInfo ci) {
        Entity cameraEntity = camera.getFocusedEntity();
        if (cameraEntity instanceof RequiemPlayer) {
            requiem_camerasFocused = RequiemFx.INSTANCE.getAnimationEntity();
        } else {
            requiem_camerasFocused = null;
        }
    }

    @ModifyVariable(
        method = "renderEntity",
        at = @At("HEAD")
    )
    private VertexConsumerProvider swapRenderLayer(VertexConsumerProvider baseRenderLayer,
                                                   Entity entity) {
        if (entity == requiem_camerasFocused) {
            RequiemVertexConsumerProvider requiemVertexConsumers = RequiemBuilderStorage.INSTANCE.getRequiemVertexConsumers();
            // Performance note: technically unnecessary renderer lookup,
            // but it is easier to swap the vertex consumers early.
            Identifier entityTexture = this.entityRenderDispatcher.getRenderer(entity).getTexture(entity);
            requiemVertexConsumers.setLayer(RequiemRenderLayers.getZoomFx(entityTexture));
            return requiemVertexConsumers;
        }
        return baseRenderLayer;
    }
}
