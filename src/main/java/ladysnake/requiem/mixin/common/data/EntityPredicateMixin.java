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
package ladysnake.requiem.mixin.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPredicate.class)
public abstract class EntityPredicateMixin {
    private @Nullable Boolean requiem$canBeCured;
    private NumberRange.FloatRange requiem$healthFraction = NumberRange.FloatRange.ANY;

    @Inject(method = "test(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    private void test(ServerWorld world, Vec3d pos, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof LivingEntity) {
            if (requiem$canBeCured != null) {
                PlayerEntity possessor = ((ProtoPossessable) entity).getPossessor();
                if (possessor != null && RemnantComponent.KEY.get(possessor).canCurePossessed((LivingEntity) entity) != requiem$canBeCured) {
                    cir.setReturnValue(false);
                    return;
                }
            }
            if (!requiem$healthFraction.test(((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth())) {
                cir.setReturnValue(false);
            }
        }
    }

    // ANY return is actually an early return in the bytecode
    @Inject(method = "fromJson", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void fromJson(JsonElement json, CallbackInfoReturnable<EntityPredicate> cir, JsonObject entityData) {
        //noinspection ConstantConditions
        EntityPredicateMixin ret = (EntityPredicateMixin) (Object) cir.getReturnValue();

        if (entityData.has("requiem:can_be_cured")) {
            ret.requiem$canBeCured
                = JsonHelper.getBoolean(entityData, "requiem:can_be_cured");
        }
        ret.requiem$healthFraction = NumberRange.FloatRange.fromJson(entityData.get("requiem:health_fraction"));
    }
}
