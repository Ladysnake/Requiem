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
package ladysnake.requiem.mixin.block.entity;

import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.item.WrittenOpusItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin extends BlockEntity {
    @Shadow private ItemStack book;

    public LecternBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "hasBook", at = @At("RETURN"), cancellable = true)
    private void hasBook(CallbackInfoReturnable<Boolean> cir) {
        Item bookItem = this.book.getItem();
        if (bookItem instanceof OpusDemoniumItem || bookItem instanceof WrittenOpusItem) {
            cir.setReturnValue(true);
        }
    }
}
