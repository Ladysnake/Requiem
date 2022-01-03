/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.mixin.client.attrition;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.client.RequiemClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T> {
    @Unique
    private StatusEffectInstance renderedEffect;
    @Unique
    private boolean boundSpecialBackground;

    public AbstractInventoryScreenMixin(T container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    // ModifyVariable is only used to capture the local variable more easily
    // we cannot use INVOKE_ASSIGN and Iterator#next, because there is a hidden cast instruction
    @ModifyVariable(method = "drawStatusEffectBackgrounds", at = @At(value = "STORE"))
    private StatusEffectInstance customizeDrawnBackground(StatusEffectInstance effect) {
        if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
            assert client != null;
            RenderSystem.setShaderTexture(0, RequiemClient.SOULBOUND_BACKGROUND);
            boundSpecialBackground = true;
        }
        return effect;
    }

    @Inject(method = "drawStatusEffectBackgrounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", shift = At.Shift.AFTER))
    private void restoreDrawnBackground(MatrixStack matrices, int x, int height, Iterable<StatusEffectInstance> statusEffects, boolean bl, CallbackInfo ci) {
        if (boundSpecialBackground) {
            assert client != null;
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            boundSpecialBackground = false;
        }
    }

    @ModifyVariable(method = "drawStatusEffectSprites", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getEffectType()Lnet/minecraft/entity/effect/StatusEffect;"))
    private StatusEffectInstance captureCurrentEffect(StatusEffectInstance effect) {
        this.renderedEffect = effect;
        return effect;
    }

    @ModifyVariable(method = "drawStatusEffectSprites", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/texture/StatusEffectSpriteManager;getSprite(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite customizeDrawnSprite(Sprite baseSprite) {
        return RequiemClient.instance().statusEffectSpriteManager().substituteSprite(baseSprite, renderedEffect);
    }

}
