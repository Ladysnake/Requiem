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
import ladysnake.requiem.mixin.common.possession.gameplay.ability.BlazeEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlazeFireballAbility extends IndirectAbilityBase<LivingEntity> {
    public static final double RANDOM_SKEW_FACTOR = MathHelper.sqrt(MathHelper.sqrt(25.0f)) * 0.25F;
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
            if (this.owner instanceof BlazeEntity && ((BlazeEntityAccessor) this.owner).requiem$invokeIsFireActive()) {
                ((BlazeEntityAccessor) this.owner).requiem$invokeSetFireActive(false);
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
        fireball.setPosition(this.owner.getX(), this.owner.getY() + (double)(this.owner.getHeight() / 2.0F) + 0.5D, this.owner.getZ());
        this.owner.world.spawnEntity(fireball);
    }

    private void playFireballEffects() {
        this.owner.world.syncWorldEvent(BLAZE_SHOOT_EVENT, this.owner.getBlockPos(), 0);
        if (this.owner instanceof BlazeEntity) {
            ((BlazeEntityAccessor) this.owner).requiem$invokeSetFireActive(true);
        }
    }

    private void consumeFireball() {
        this.fireballs--;

        if (this.fireballs == 0) {
            this.beginCooldown();
        }
    }
}
