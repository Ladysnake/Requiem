package ladysnake.pandemonium.mixin.client.render;

import ladysnake.pandemonium.client.PandemoniumClient;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/VisibleRegion;F)V", shift = AFTER))
    private void renderSpecials(float tickDelta, long time, CallbackInfo ci) {
        Entity camera = this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity();
        if (camera instanceof PlayerEntity && RequiemPlayer.from((PlayerEntity) camera).asRemnant().isIncorporeal()) {
            PandemoniumClient.INSTANCE.soulWebRenderer.render(tickDelta, time);
        }
    }
}
