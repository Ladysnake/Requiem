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
package ladysnake.requiem.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RangedAttackAbility<T extends MobEntity & RangedAttackMob> extends IndirectAbilityBase<T> {

    public RangedAttackAbility(T owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        double range = 64.0;
        Vec3d startPoint = this.owner.getCameraPosVec(1.0f);
        Vec3d lookVec = this.owner.getRotationVec(1.0f);
        Vec3d endPoint = startPoint.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        Vec3d vec3d_2 = this.owner.getRotationVec(1.0F);
        Box boundingBox_1 = this.owner.getBoundingBox().stretch(vec3d_2.x * range, vec3d_2.y * range, vec3d_2.z * range).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult trace = ProjectileUtil.rayTrace(this.owner, startPoint, endPoint, boundingBox_1, (entity_1x) -> entity_1x != player && !entity_1x.isSpectator() && entity_1x.collides(), range);
        if (trace != null) {
            Entity traced = trace.getEntity();
            if (traced instanceof LivingEntity) {
                this.owner.attack((LivingEntity) traced, 1f);
            }
        }
        return false;
    }
}
