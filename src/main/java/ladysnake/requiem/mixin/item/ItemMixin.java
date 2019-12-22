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

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        MobEntity possessedEntity = ((RequiemPlayer) player).asPossessor().getPossessedEntity();
        ItemStack heldStack = player.getStackInHand(hand);
        if (possessedEntity != null) {
            if (possessedEntity.isUndead() && RequiemItemTags.UNDEAD_CURES.contains(heldStack.getItem()) && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
                player.setCurrentHand(hand);
                cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, heldStack));
            } else if (RequiemEntityTypeTags.ZOMBIES.contains(possessedEntity.getType())) {
                if (RequiemItemTags.RAW_MEATS.contains(heldStack.getItem()) || RequiemItemTags.RAW_FISHES.contains(heldStack.getItem()) && possessedEntity instanceof DrownedEntity) {
                    player.setCurrentHand(hand);
                    cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, heldStack));
                } else {
                    cir.setReturnValue(new TypedActionResult<>(ActionResult.FAIL, heldStack));
                }
            } else if (RequiemEntityTypeTags.SKELETONS.contains(possessedEntity.getType())) {
                if (possessedEntity.getHealth() < possessedEntity.getMaximumHealth()) {
                    possessedEntity.heal(4.0f);
                    possessedEntity.playAmbientSound();
                    heldStack.decrement(1);
                    player.getItemCooldownManager().set(heldStack.getItem(), 40);
                }
            }
        }
    }
}
