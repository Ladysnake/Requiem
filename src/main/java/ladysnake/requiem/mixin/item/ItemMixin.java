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

import ladysnake.requiem.api.v1.player.RequiemPlayer;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        MobEntity possessedEntity = ((RequiemPlayer) player).getPossessionComponent().getPossessedEntity();
        ItemStack stack = player.getStackInHand(hand);
        if (possessedEntity != null && possessedEntity.isUndead() && RequiemItemTags.UNDEAD_CURES.contains(stack.getItem()) && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            player.setCurrentHand(hand);
            cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, stack));
        } else if (possessedEntity instanceof ZombieEntity) {
            if (RequiemItemTags.RAW_MEATS.contains(stack.getItem()) || ItemTags.FISHES.contains(stack.getItem()) && possessedEntity instanceof DrownedEntity) {
                player.setCurrentHand(hand);
                cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, stack));
            } else {
                cir.setReturnValue(new TypedActionResult<>(ActionResult.FAIL, stack));
            }
        }
    }
}
