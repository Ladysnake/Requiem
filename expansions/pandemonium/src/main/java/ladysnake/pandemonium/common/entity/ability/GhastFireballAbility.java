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
package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.Vec3d;

public class GhastFireballAbility extends IndirectAbilityBase<MobEntity> {
    private int fireballCooldown = 0;

    public GhastFireballAbility(MobEntity owner) {
        super(owner, 0);
    }

    @Override
    public void update(int cooldown) {
        if (this.fireballCooldown > 0) {
            this.fireballCooldown--;

            if (this.owner instanceof GhastEntity) {
                ((GhastEntity) this.owner).setShooting(this.fireballCooldown > 20);
            }
        }
    }

    @Override
    public Result trigger() {
        if (this.fireballCooldown == 0) {
            if (!this.owner.world.isClient) {
                Vec3d scaledRot = this.owner.getRotationVec(1.0F);
                Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
                this.owner.world.syncWorldEvent(null, 1016, this.owner.getBlockPos(), 0);
                FireballEntity fireball = new FireballEntity(this.owner.world, this.owner, rot.x, rot.y, rot.z);
                fireball.explosionPower = this.owner instanceof GhastEntity ? ((GhastEntity) this.owner).getFireballStrength() : 1;
                fireball.updatePosition(
                    this.owner.getX() + scaledRot.x * 4.0D,
                    this.owner.getY() + (double) (this.owner.getHeight() / 2.0F) + 0.5D,
                    this.owner.getZ() + scaledRot.z * 4.0D
                );
                this.owner.world.spawnEntity(fireball);
            }
            this.fireballCooldown = 60;
            return Result.SUCCESS;
        }
        return Result.FAIL;
    }
}
