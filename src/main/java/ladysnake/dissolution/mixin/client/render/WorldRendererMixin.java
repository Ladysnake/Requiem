package ladysnake.dissolution.mixin.client.render;

import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.Possessor;
import net.minecraft.class_856;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void preRenderEntities(Entity camera, class_856 frustum, float tickDelta, CallbackInfo info) {
        if (camera instanceof Possessor) {
            Possessable possessed = ((Possessor) camera).getPossessedEntity();
            if (possessed != null) {
                MinecraftClient.getInstance().setCameraEntity((Entity) possessed);
            }
        }
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    public void postRenderEntities(Entity camera, class_856 frustum, float tickDelta, CallbackInfo info) {
        if (camera instanceof Possessor) {
            Possessable possessed = ((Possessor) camera).getPossessedEntity();
            if (possessed == client.getCameraEntity()) {
                client.setCameraEntity(camera);
            }
        }
    }
}
