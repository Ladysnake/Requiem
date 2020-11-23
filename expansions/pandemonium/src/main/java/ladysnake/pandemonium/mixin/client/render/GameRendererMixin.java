package ladysnake.pandemonium.mixin.client.render;

import ladysnake.pandemonium.common.entity.WololoComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
        method = "onCameraEntitySet",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/render/GameRenderer;shader:Lnet/minecraft/client/gl/ShaderEffect;",
            shift = At.Shift.AFTER
        ),
        require = 0,       // optibad compatibility
        cancellable = true
    )
    private void useCustomEntityShader(@Nullable Entity entity, CallbackInfo info) {
        if (entity != null && WololoComponent.isConverted(entity)) {
            info.cancel();
        }
    }
}
