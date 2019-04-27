/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.mixin.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.client.HotbarRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    private static final Identifier POSSESSION_ICON = Requiem.id("textures/gui/possession_icon.png");

    @Shadow @Final private MinecraftClient client;

    @Shadow @Nullable protected abstract PlayerEntity getCameraPlayer();

    @Shadow protected abstract int method_1744(LivingEntity livingEntity_1);

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"), cancellable = true)
    private void colorCrosshair(CallbackInfo ci) {
        if (((RequiemPlayer) this.client.player).getRemnantState().isIncorporeal()) {
            Entity targetedEntity = this.client.targetedEntity;
            if (targetedEntity instanceof MobEntity) {
                if (targetedEntity instanceof EndermanEntity && this.client.player.dimension == DimensionType.THE_END) {
                    GlStateManager.color3f(0.4f, 0.0f, 1.0f);
                }
                int x = (this.scaledWidth - 32) / 2;
                int y = (this.scaledHeight - 16) / 2;
                this.client.getTextureManager().bindTexture(POSSESSION_ICON);
                DrawableHelper.blit(x, y, 32, 32, 0, 0, 16, 16, 16, 16);
                this.client.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    private void drawPossessionHud(CallbackInfo info) {
        if (((RequiemPlayer)client.player).getRemnantState().isIncorporeal()) {
            // Make everything that follows *invisible*
            GlStateManager.color4f(1, 1, 1, 0);
        }
    }

    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;method_1744(Lnet/minecraft/entity/LivingEntity;)I"
            )
    )
    private int preventFoodRender(InGameHud self, LivingEntity livingEntity_1) {
        int actual = this.method_1744(livingEntity_1);
        RequiemPlayer cameraPlayer = (RequiemPlayer) this.getCameraPlayer();
        if (actual == 0 && cameraPlayer != null && cameraPlayer.getRemnantState().isSoul()) {
            return -1;
        }
        return actual;
    }

    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(from = @At(value = "CONSTANT", args="stringValue=air")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInFluid(Lnet/minecraft/tag/Tag;)Z")
    )
    private boolean preventAirRender(PlayerEntity playerEntity, Tag<Fluid> fluid) {
        if (((RequiemPlayer)playerEntity).getRemnantState().isSoul()) {
            LivingEntity possessed = ((RequiemPlayer) playerEntity).getPossessionComponent().getPossessedEntity();
            if (possessed == null) {
                return false;
            } else if (possessed.canBreatheInWater()) {
                return false;
            }
        }
        return playerEntity.isInFluid(fluid);
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemUtil;getMeasuringTimeMs()J"), ordinal = 0)
    private int substituteHealth(int health) {
        LivingEntity entity = ((RequiemPlayer)client.player).getPossessionComponent().getPossessedEntity();
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
        if (((RequiemPlayer)client.player).getRemnantState().isSoul()) {
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

}
