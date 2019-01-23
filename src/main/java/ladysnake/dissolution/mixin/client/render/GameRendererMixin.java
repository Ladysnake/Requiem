package ladysnake.dissolution.mixin.client.render;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static org.spongepowered.asm.mixin.injection.At.Shift.BY;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow public abstract ShaderEffect getShader();

    @Inject(
            method = "updateTargetedEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;" +
                            "getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BoundingBox;Ljava/util/function/Predicate;)" +
                            "Ljava/util/List;",
                    shift = BY,
                    by = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void unselectPossessedEntity(float tickDelta, CallbackInfo info,
                                        Entity camera, double double_1, Vec3d vec3d_1, boolean boolean_1, int int_1, double double_2, Vec3d vec3d_2, Vec3d vec3d_3, Vec3d vec3d_4, float float_2,
                                        List<Entity> entities) {
        if (camera instanceof DissolutionPlayer && ((DissolutionPlayer) camera).getPossessionManager().isPossessing()) {
            // Possessable are still entities
            //noinspection SuspiciousMethodCalls
            entities.remove(((DissolutionPlayer) camera).getPossessionManager().getPossessedEntity());
        }
    }
}
