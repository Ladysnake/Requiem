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

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;

public class ShulkerShootAbility extends IndirectAbilityBase<ShulkerEntity> implements DirectAbility<ShulkerEntity> {
    private int bulletCooldown = 20;

    public ShulkerShootAbility(ShulkerEntity owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (this.bulletCooldown <= 0) {
            // method_21727 = getClosestEntity
            return this.trigger(player, this.owner.world.getClosestEntityIncludingUngeneratedChunks(
                    LivingEntity.class,
                    new TargetPredicate(),
                    this.owner,
                    this.owner.getX(),
                    this.owner.getY() + (double)this.owner.getStandingEyeHeight(),
                    this.owner.getZ(),
                    this.getSearchBox(16.0)));
        }
        return false;
    }

    @Override
    public boolean trigger(PlayerEntity player, @Nullable Entity target) {
        if (this.bulletCooldown <= 0 && target instanceof LivingEntity) {
            this.owner.world.spawnEntity(new ShulkerBulletEntity(this.owner.world, this.owner, target, this.owner.getAttachedFace().getAxis()));
            this.owner.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (this.owner.world.random.nextFloat() - this.owner.world.random.nextFloat()) * 0.2F + 1.0F);
            this.bulletCooldown = 20;
        }

        return false;
    }

    @Override
    public void update() {
        this.bulletCooldown--;
    }

    private Box getSearchBox(double range) {
        return this.owner.getBoundingBox().expand(range, 4.0D, range);
    }

}
