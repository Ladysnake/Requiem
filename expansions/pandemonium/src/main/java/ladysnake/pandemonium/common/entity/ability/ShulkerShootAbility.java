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
import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;

public class ShulkerShootAbility implements IndirectAbility<ShulkerEntity>, DirectAbility<ShulkerEntity> {
    private final ShulkerEntity shulker;
    private int bulletCooldown = 20;

    public ShulkerShootAbility(ShulkerEntity shulker) {
        this.shulker = shulker;
    }

    @Override
    public double getRange() {
        return 16;
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (this.bulletCooldown <= 0) {
            // method_21727 = getClosestEntity
            return this.trigger(player, this.shulker.world.getClosestEntityIncludingUngeneratedChunks(
                    LivingEntity.class,
                    new TargetPredicate(),
                    this.shulker,
                    this.shulker.getX(),
                    this.shulker.getY() + (double)this.shulker.getStandingEyeHeight(),
                    this.shulker.getZ(),
                    this.getSearchBox()));
        }
        return false;
    }

    @Override
    public boolean trigger(PlayerEntity player, @Nullable Entity target) {
        if (target instanceof LivingEntity && this.shulker.getPeekAmount() > 50) {
            if (player.world.isClient) return true;

            if (this.bulletCooldown <= 0) {
                this.shulker.world.spawnEntity(new ShulkerBulletEntity(this.shulker.world, this.shulker, target, this.shulker.getAttachedFace().getAxis()));
                this.shulker.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (this.shulker.world.random.nextFloat() - this.shulker.world.random.nextFloat()) * 0.2F + 1.0F);
                this.bulletCooldown = 20;
                return true;
            }
        }

        return false;
    }

    @Override
    public void update() {
        if (this.bulletCooldown > 0) {
            this.bulletCooldown--;
        }
    }

    private Box getSearchBox() {
        double range = this.getRange();
        return this.shulker.getBoundingBox().expand(range, 4.0D, range);
    }

}
