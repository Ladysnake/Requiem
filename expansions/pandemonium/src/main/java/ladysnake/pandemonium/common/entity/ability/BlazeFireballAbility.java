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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlazeFireballAbility extends IndirectAbilityBase<LivingEntity> {
    public static final double RANDOM_SKEW_FACTOR = MathHelper.sqrt(MathHelper.sqrt(25.0)) * 0.25F;
    public static final int CONSECUTIVE_FIREBALLS = 3;
    public static final int FIRE_TICKS = 200;
    public static final int BLAZE_SHOOT_EVENT = 1018;

    private int fireballs = CONSECUTIVE_FIREBALLS;

    public BlazeFireballAbility(LivingEntity owner) {
        super(owner, FIRE_TICKS);
    }

    @Override
    public void onCooldownEnd() {
        if (!this.owner.world.isClient) {
            if (this.owner instanceof BlazeEntity && ((BlazeEntityAccessor) this.owner).invokeIsFireActive()) {
                ((BlazeEntityAccessor) this.owner).invokeSetFireActive(false);
            }
            this.fireballs = CONSECUTIVE_FIREBALLS;
        }
    }

    @Override
    public boolean run() {
        if (!this.owner.world.isClient && this.fireballs > 0) {
            this.playFireballEffects();
            this.spawnFireball();
            this.consumeFireball();
        }
        return true;
    }

    private void spawnFireball() {
        Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
        SmallFireballEntity fireball = new SmallFireballEntity(
                this.owner.world,
                this.owner,
                rot.x + this.owner.getRandom().nextGaussian() * RANDOM_SKEW_FACTOR,
                rot.y,
                rot.z + this.owner.getRandom().nextGaussian() * RANDOM_SKEW_FACTOR
        );
        fireball.updatePosition(this.owner.getX(), this.owner.getY() + (double)(this.owner.getHeight() / 2.0F) + 0.5D, this.owner.getZ());
        this.owner.world.spawnEntity(fireball);
    }

    private void playFireballEffects() {
        this.owner.world.syncWorldEvent(BLAZE_SHOOT_EVENT, this.owner.getBlockPos(), 0);
        if (this.owner instanceof BlazeEntity) {
            ((BlazeEntityAccessor) this.owner).invokeSetFireActive(true);
        }
    }

    private void consumeFireball() {
        this.fireballs--;

        if (this.fireballs == 0) {
            this.beginCooldown();
        }
    }
}
