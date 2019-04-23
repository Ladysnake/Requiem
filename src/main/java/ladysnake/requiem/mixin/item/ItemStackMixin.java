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
package ladysnake.requiem.mixin.item;

import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getTooltipText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void fireTooltipEvent(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<TextComponent>> cir, List<TextComponent> lines) {
        ItemTooltipCallback.EVENT.invoker().onTooltipBuilt((ItemStack)(Object)this, player, context, lines);
    }
}
