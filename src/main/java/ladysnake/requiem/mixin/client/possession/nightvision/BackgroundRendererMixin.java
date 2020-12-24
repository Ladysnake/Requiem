package ladysnake.requiem.mixin.client.possession.nightvision;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Shadow
    private static float red;

    @Shadow
    private static float green;

    @Shadow
    private static float blue;

    @Inject(method = "render", slice = @Slice(from=@At(value = "FIELD:LAST", opcode = Opcodes.PUTSTATIC)), at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC))
    private static void render(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci) {
        if (RemnantComponent.isIncorporeal(camera.getFocusedEntity())) {
            float greyscale = red * 0.3f + green * 0.59f + blue * 0.11f;
            red = Math.max(red, greyscale);
            green = Math.max(green, greyscale);
            blue = Math.max(blue, greyscale);
        }
    }
}
