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

import com.mojang.datafixers.util.Pair;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.client.RequiemClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
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
    private static final Pair<Identifier, Identifier> LOCKED_SPRITE_REF = Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, RequiemClient.LOCKED_SLOT_SPRITE);

    @Shadow
    @Final
    private int index;
    @Unique
    protected @Nullable InventoryLimiter limiter;
    @Unique
    protected boolean craftingSlot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(Inventory inventory, int index, int x, int y, CallbackInfo ci) {
        if (inventory instanceof PlayerInventory) {
            this.limiter = InventoryLimiter.KEY.get(((PlayerInventory) inventory).player);
        } else if (inventory instanceof CraftingInventory) {
            ScreenHandler handler = ((CraftingInventoryAccessor) inventory).getHandler();
            if (handler instanceof PlayerScreenHandlerAccessor) {
                this.limiter = InventoryLimiter.KEY.get(((PlayerScreenHandlerAccessor) handler).getOwner());
                this.craftingSlot = true;
            }
        }
    }

    @Unique
    private boolean shouldBeLocked() {
        return this.limiter != null && ((this.craftingSlot && this.limiter.isLocked(InventoryPart.CRAFTING)) || this.limiter.isSlotLocked(this.index));
    }

    @Unique
    private boolean shouldBeInvisible() {
        return this.limiter != null && ((this.craftingSlot && this.limiter.isLocked(InventoryPart.CRAFTING)) || this.limiter.isSlotInvisible(this.index));
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeLocked()) cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeLocked()) cir.setReturnValue(false);
    }

    @Environment(EnvType.CLIENT)    // TODO confirm that this does not crash servers
    @Inject(method = "getBackgroundSprite", at = @At("HEAD"), cancellable = true)
    private void getLockedSprite(CallbackInfoReturnable<@Nullable Pair<Identifier, Identifier>> cir) {
        if (this.shouldBeLocked()) cir.setReturnValue(LOCKED_SPRITE_REF);
    }

    @Environment(EnvType.CLIENT)    // TODO confirm that this does not crash servers
    @Inject(method = "doDrawHoveringEffect", at = @At("HEAD"), cancellable = true)
    private void preventSpecialRender(CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeInvisible()) cir.setReturnValue(false);
    }
}
