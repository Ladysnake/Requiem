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

import ladysnake.pandemonium.mixin.common.entity.mob.BlazeEntityAccessor;
import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlazeFireballAbility extends IndirectAbilityBase<MobEntity> {
    private int fireTicks;

    public BlazeFireballAbility(MobEntity owner) {
        super(owner);
    }

    @Override
    public void update() {
        if (this.owner instanceof BlazeEntity && ((BlazeEntityAccessor) this.owner).invokeIsFireActive() && --fireTicks < 0) {
            ((BlazeEntityAccessor) this.owner).invokeSetFireActive(false);
        }
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        double d = 25.0;
        float f = MathHelper.sqrt(MathHelper.sqrt(d)) * 0.5F;
        Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);

        this.owner.world.syncWorldEvent(null, 1018, new BlockPos((int)this.owner.getX(), (int)this.owner.getY(), (int)this.owner.getZ()), 0);
        if (this.owner instanceof BlazeEntity) {
            this.fireTicks = 200;
            ((BlazeEntityAccessor) this.owner).invokeSetFireActive(true);
        }
        SmallFireballEntity fireball = new SmallFireballEntity(
                this.owner.world,
                this.owner,
                rot.x + this.owner.getRandom().nextGaussian() * (double)f,
                rot.y,
                rot.z + this.owner.getRandom().nextGaussian() * (double)f
        );
        fireball.updatePosition(this.owner.getX(), this.owner.getY() + (double)(this.owner.getHeight() / 2.0F) + 0.5D, this.owner.getZ());
        this.owner.world.spawnEntity(fireball);
        return true;
    }
}
