/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.mixin.client.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.common.impl.inventory.PlayerInventoryLimiter;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lower priority to load before Bedrockify's mixin, fix for issue Ladysnake/Requiem#198
@Mixin(value = InGameHud.class, priority = 995)
public abstract class InGameHudMixin {
    @Unique
    private static final int X_CENTER_SHIFT = 77;

    @Unique
    private boolean cancelNextItem;
    @Unique
    private boolean renderMainHandOnly;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(
        method = "renderHotbar",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;WIDGETS_TEXTURE:Lnet/minecraft/util/Identifier;"),
        cancellable = true
    )
    private void checkInventoryLimit(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        InventoryLimiter.HotbarAvailability hotbarAvailability = InventoryLimiter.KEY.get(this.getCameraPlayer()).getHotbarAvailability();
        if (hotbarAvailability == InventoryLimiter.HotbarAvailability.NONE) {
            ci.cancel();
        } else {
            this.renderMainHandOnly = hotbarAvailability == InventoryLimiter.HotbarAvailability.HANDS;
        }
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 1
    )
    private int centerCroppedHotbar(int x) {
        if (this.renderMainHandOnly) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 1),
        index = 1
    )
    private int centerSelectedSlot(int x) {
        if (this.renderMainHandOnly) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 5
    )
    private int cropHotbar_width(int width) {
        if (this.renderMainHandOnly) {
            return 21;
        }
        return width;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;")
    )
    private int cancelLockedItemRender(int index) {
        this.cancelNextItem = renderMainHandOnly && index != PlayerInventoryLimiter.MAINHAND_SLOT;
        return index;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
    )
    private ItemStack cancelLockedItemRender(ItemStack stack) {
        if (this.cancelNextItem) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V"),
        index = 0
    )
    private int shiftMainHandItem(int x) {
        if (this.renderMainHandOnly && !this.cancelNextItem) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }
}
