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
    public GhastFireballAbility(MobEntity owner) {
        super(owner, 60);
    }

    @Override
    public void update() {
        super.update();
        if (this.cooldown == 20) {
            this.setShooting(false);
        }
    }

    private void setShooting(boolean shooting) {
        if (this.owner instanceof GhastEntity) {
            ((GhastEntity) this.owner).setShooting(shooting);
        }
    }

    @Override
    public boolean run() {
        if (!this.owner.world.isClient) {
            this.owner.world.syncWorldEvent(null, 1016, this.owner.getBlockPos(), 0);
            this.setShooting(true);
            this.spawnFireball();
            this.beginCooldown();
        }
        return true;
    }

    private void spawnFireball() {
        Vec3d scaledRot = this.owner.getRotationVec(1.0F);
        Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
        FireballEntity fireball = new FireballEntity(this.owner.world, this.owner, rot.x, rot.y, rot.z);
        fireball.explosionPower = this.owner instanceof GhastEntity ? ((GhastEntity) this.owner).getFireballStrength() : 1;
        fireball.updatePosition(
            this.owner.getX() + scaledRot.x * 4.0D,
            this.owner.getY() + (double) (this.owner.getHeight() / 2.0F) + 0.5D,
            this.owner.getZ() + scaledRot.z * 4.0D
        );
        this.owner.world.spawnEntity(fireball);
    }
}
