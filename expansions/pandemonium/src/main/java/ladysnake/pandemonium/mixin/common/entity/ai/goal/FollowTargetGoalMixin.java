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
package ladysnake.pandemonium.mixin.common.entity.ai.goal;

import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BoundingBox;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(FollowTargetGoal.class)
public abstract class FollowTargetGoalMixin extends TrackTargetGoal {
    @Shadow @Nullable protected LivingEntity targetEntity;

    @Shadow protected TargetPredicate targetPredicate;

    @Shadow protected abstract BoundingBox getSearchBox(double double_1);

    public FollowTargetGoalMixin(MobEntity mobEntity_1, boolean boolean_1) {
        super(mobEntity_1, boolean_1);
    }

    @Inject(
            method = "findClosestTarget",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/ai/goal/FollowTargetGoal;targetEntity:Lnet/minecraft/entity/LivingEntity;",
                    ordinal = 1,
                    shift = AFTER
            )
    )
    private void addShellsAsTargets(CallbackInfo ci) {
        if (this.targetEntity == null) {
            this.targetEntity = this.mob.world.getClosestEntity(PlayerShellEntity.class, this.targetPredicate, this.mob, this.mob.x, this.mob.y + (double)this.mob.getStandingEyeHeight(), this.mob.z, this.getSearchBox(this.getFollowRange()));
        }
    }
}
