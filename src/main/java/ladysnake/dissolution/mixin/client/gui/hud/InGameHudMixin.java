package ladysnake.dissolution.mixin.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.client.HudEvent;
import ladysnake.dissolution.api.v1.remnant.RemnantHandler;
import ladysnake.dissolution.client.gui.hud.PossessionHud;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin extends Drawable {

    @Shadow @Final private MinecraftClient client;

    @Shadow private long field_2032;

    @Shadow private int ticks;

    @Shadow private int field_2033;

    @Inject(
            method = "method_1760",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    public void drawPossessionHud(CallbackInfo info) {
        RemnantHandler handler = ((DissolutionPlayer)client.player).getRemnantHandler();
        if (handler != null && handler.isSoul()) {
            boolean highlight = this.field_2032 > (long) this.ticks && (this.field_2032 - (long) this.ticks) / 3L % 2L == 1L;
            PossessionHud.INSTANCE.draw(this.field_2033, this.ticks, highlight);
            // Make everything that follows *invisible*
            GlStateManager.color4f(1, 1, 1, 0);
        }
    }

    @Inject(method = "method_1759", at = @At("HEAD"), cancellable = true)
    public void fireHotBarRenderEvent(float tickDelta, CallbackInfo info) {
        for (Float2ObjectFunction<ActionResult> handler : ((HandlerArray<Float2ObjectFunction<ActionResult>>) HudEvent.RENDER_HOTBAR).getBackingArray()) {
            if (handler.get(tickDelta) != ActionResult.PASS) {
                info.cancel();
            }
        }
    }

    @Inject(
            method = "method_1760",
            at = @At(value = "CONSTANT", args = "stringValue=air")
    )
    public void resumeDrawing(CallbackInfo info) {
        RemnantHandler handler = ((DissolutionPlayer)client.player).getRemnantHandler();
        if (handler != null && handler.isSoul()) {
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

}
