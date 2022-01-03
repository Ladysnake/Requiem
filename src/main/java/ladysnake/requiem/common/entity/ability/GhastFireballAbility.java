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
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.core.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.Vec3d;

public class GhastFireballAbility extends IndirectAbilityBase<LivingEntity> {
    public GhastFireballAbility(LivingEntity owner) {
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
        int explosionPower = this.owner instanceof GhastEntity ? ((GhastEntity) this.owner).getFireballStrength() : 1;
        FireballEntity fireball = new FireballEntity(this.owner.world, this.owner, rot.x, rot.y, rot.z, explosionPower);
        fireball.setPosition(
            this.owner.getX() + scaledRot.x * 4.0D,
            this.owner.getY() + (double) (this.owner.getHeight() / 2.0F) + 0.5D,
            this.owner.getZ() + scaledRot.z * 4.0D
        );
        this.owner.world.spawnEntity(fireball);
    }
}
