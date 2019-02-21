package ladysnake.dissolution.mixin.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.client.HotbarRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    private void drawPossessionHud(CallbackInfo info) {
        if (((DissolutionPlayer)client.player).getRemnantState().isIncorporeal()) {
            // Make everything that follows *invisible*
            GlStateManager.color4f(1, 1, 1, 0);
        }
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemUtil;getMeasuringTimeMs()J"), ordinal = 0)
    private int substituteHealth(int health) {
        LivingEntity entity = (LivingEntity) ((DissolutionPlayer)client.player).getPossessionComponent().getPossessedEntity();
        if (entity != null) {
            return MathHelper.ceil(entity.getHealth());
        }
        return health;
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void fireHotBarRenderEvent(float tickDelta, CallbackInfo info) {
        if (HotbarRenderCallback.EVENT.invoker().onHotbarRendered(tickDelta) != ActionResult.PASS) {
            info.cancel();
        }
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=air")
    )
    private void resumeDrawing(CallbackInfo info) {
        if (((DissolutionPlayer)client.player).getRemnantState().isSoul()) {
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

}
