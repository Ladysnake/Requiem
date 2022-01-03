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
package ladysnake.requiem.mixin.common.inventory;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow
    @Final
    private int index;
    protected @Nullable PlayerEntity requiem$player;
    protected @Nullable InventoryLimiter requiem$limiter;
    @Unique
    protected boolean craftingSlot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(Inventory inventory, int index, int x, int y, CallbackInfo ci) {
        if (inventory instanceof PlayerInventory) {
            this.requiem$player = ((PlayerInventory) inventory).player;
        } else if (inventory instanceof CraftingInventory) {
            ScreenHandler handler = ((CraftingInventoryAccessor) inventory).requiem$getHandler();
            if (handler instanceof PlayerScreenHandlerAccessor) {
                this.requiem$player = ((PlayerScreenHandlerAccessor) handler).getOwner();
                this.craftingSlot = true;
            }
        }
    }

    @Unique
    private boolean shouldBeInvisible() {
        if (this.requiem$player == null) return false;
        InventoryLimiter limiter = InventoryLimiter.instance();
        return limiter.getInventoryShape(requiem$player) != InventoryShape.NORMAL
            && (this.craftingSlot
            ? limiter.isLocked(this.requiem$player, DefaultInventoryNodes.CRAFTING)
            : limiter.isSlotInvisible(requiem$player, this.index));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true)
    private void preventSpecialRender(CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeInvisible()) cir.setReturnValue(false);
    }
}
