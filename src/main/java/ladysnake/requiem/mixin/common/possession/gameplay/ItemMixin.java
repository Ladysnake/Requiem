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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.entity.SkeletonBoneComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        PossessionComponent possessionComponent = PossessionComponent.get(player);
        MobEntity possessedEntity = possessionComponent.getPossessedEntity();
        if (possessedEntity != null) {
            ItemStack heldStack = player.getStackInHand(hand);
            StatusEffectInstance hungry = player.getStatusEffect(StatusEffects.HUNGER);
            if (possessionComponent.canBeCured(heldStack)) {
                player.setCurrentHand(hand);
                cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, heldStack));
            } else if (RequiemEntityTypeTags.ZOMBIES.contains(possessedEntity.getType()) && hungry == null) {
                if (RequiemItemTags.RAW_MEATS.contains(heldStack.getItem()) || RequiemItemTags.RAW_FISHES.contains(heldStack.getItem()) && possessedEntity instanceof DrownedEntity) {
                    player.setCurrentHand(hand);
                    cir.setReturnValue(new TypedActionResult<>(ActionResult.SUCCESS, heldStack));
                } else {
                    cir.setReturnValue(new TypedActionResult<>(ActionResult.FAIL, heldStack));
                }
            } else if (RequiemEntityTypeTags.SKELETONS.contains(possessedEntity.getType()) && hungry == null) {
                if (RequiemItemTags.BONES.contains(heldStack.getItem())) {
                    if (SkeletonBoneComponent.KEY.get(possessedEntity).replaceBone()) {
                        heldStack.decrement(1);
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.HUNGER,
                            600,
                            0,
                            false,
                            false,
                            true
                        ));
                        player.getItemCooldownManager().set(heldStack.getItem(), 40);
                    }
                }
            }
        }
    }

    @Inject(method = "useOnEntity", at = @At("RETURN"), cancellable = true)
    private void useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity possessor = ((Possessable) entity).getPossessor();
        if (possessor != null && PossessionComponent.get(possessor).canBeCured(stack)) {
            if (!user.abilities.creativeMode) {
                stack.decrement(1);
            }

            PossessionComponent.KEY.get(possessor).startCuring();
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
