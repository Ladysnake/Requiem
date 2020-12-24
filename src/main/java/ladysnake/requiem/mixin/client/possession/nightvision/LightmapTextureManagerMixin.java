package ladysnake.requiem.mixin.client.possession.nightvision;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyVariable(
        method = "update",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/Vector3f;<init>(FFF)V", ordinal = 0)
        ),
        at = @At("STORE"),
        index = 5
    )
    private float getNightVisionStrength(float base, float tickDelta) {
        ClientPlayerEntity player = this.client.player;
        assert player != null;
        if (RemnantComponent.isIncorporeal(player)) {
            return GameRenderer.getNightVisionStrength(player, tickDelta);
        }
        return base;
    }
}
