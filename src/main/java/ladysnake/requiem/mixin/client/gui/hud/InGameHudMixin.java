/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.HotbarRenderCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.mixin.client.texture.SpriteAtlasHolderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;
    @Unique private boolean boundSpecialBackground;
    @Unique private StatusEffectInstance renderedEffect;

    @Shadow @Nullable protected abstract PlayerEntity getCameraPlayer();

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Shadow
    protected abstract int getHeartCount(LivingEntity livingEntity_1);

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;)V"), cancellable = true)
    private void colorCrosshair(CallbackInfo ci) {
        CrosshairRenderCallback.EVENT.invoker().onCrosshairRender(this.scaledWidth, this.scaledHeight);
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    private void drawPossessionHud(CallbackInfo info) {
        assert client.player != null;
        if (((RequiemPlayer)client.player).asRemnant().isIncorporeal()) {
            // Make everything that follows *invisible*
            RenderSystem.color4f(1, 1, 1, 0);
        }
    }

    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
            )
    )
    private int preventFoodRender(InGameHud self, LivingEntity livingEntity_1) {
        int actual = this.getHeartCount(livingEntity_1);
        RequiemPlayer cameraPlayer = (RequiemPlayer) this.getCameraPlayer();
        if (actual == 0 && cameraPlayer != null && cameraPlayer.asRemnant().isSoul()) {
            Possessable possessed = (Possessable) cameraPlayer.asPossessor().getPossessedEntity();
            if (possessed == null || !possessed.isRegularEater()) {
                return -1;
            }
        }
        return actual;
    }

    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(from = @At(value = "CONSTANT", args="stringValue=air")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInFluid(Lnet/minecraft/tag/Tag;)Z")
    )
    private boolean preventAirRender(PlayerEntity playerEntity, Tag<Fluid> fluid) {
        if (((RequiemPlayer)playerEntity).asRemnant().isSoul()) {
            LivingEntity possessed = ((RequiemPlayer) playerEntity).asPossessor().getPossessedEntity();
            if (possessed == null) {
                return false;
            } else if (possessed.canBreatheInWater()) {
                return false;
            }
        }
        return playerEntity.isInFluid(fluid);
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"), ordinal = 0)
    private int substituteHealth(int health) {
        assert client.player != null;
        LivingEntity entity = ((RequiemPlayer)client.player).asPossessor().getPossessedEntity();
        if (entity != null) {
            return MathHelper.ceil(entity.getHealth());
        }
        return health;
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void fireHotBarRenderEvent(float tickDelta, CallbackInfo info) {
        if (HotbarRenderCallback.EVENT.invoker().onHotbarRender(tickDelta) != ActionResult.PASS) {
            info.cancel();
        }
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=air")
    )
    private void resumeDrawing(CallbackInfo info) {
        assert client.player != null;
        if (((RequiemPlayer)client.player).asRemnant().isSoul()) {
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    // ModifyVariable is only used to capture the local variable more easily
    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;blit(IIIIII)V"))
    private StatusEffectInstance customizeDrawnBackground(StatusEffectInstance effect) {
        if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
            assert this.client != null;
            this.client.getTextureManager().bindTexture(AttritionStatusEffect.ATTRITION_BACKGROUND);
            boundSpecialBackground = true;
        }
        renderedEffect = effect;
        return effect;
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;blit(IIIIII)V", shift = At.Shift.AFTER))
    private void restoreDrawnBackground(CallbackInfo ci) {
        if (boundSpecialBackground) {
            this.client.getTextureManager().bindTexture(ContainerScreen.BACKGROUND_TEXTURE);
            boundSpecialBackground = false;
        }
    }

    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/texture/StatusEffectSpriteManager;getSprite(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite customizeDrawnSprite(Sprite baseSprite) {
        int amplifier = renderedEffect.getAmplifier();
        if (this.renderedEffect.getEffectType() == RequiemStatusEffects.ATTRITION && amplifier < 4) {
            Identifier baseId = baseSprite.getId();
            return ((SpriteAtlasHolderAccessor) MinecraftClient.getInstance().getStatusEffectSpriteManager())
                .getAtlas().getSprite(new Identifier(baseId.getNamespace(), baseId.getPath() + '_' + (amplifier + 1)));
        }
        return baseSprite;
    }

}
