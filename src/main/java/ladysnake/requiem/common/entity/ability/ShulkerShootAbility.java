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

import ladysnake.requiem.core.entity.ability.DirectAbilityBase;
import ladysnake.requiem.mixin.common.access.ShulkerEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.sound.SoundEvents;

public class ShulkerShootAbility extends DirectAbilityBase<ShulkerEntity, LivingEntity> {
    public static final int BULLET_COOLDOWN = 20;

    public ShulkerShootAbility(ShulkerEntity owner) {
        super(owner, BULLET_COOLDOWN, 16, LivingEntity.class);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return super.canTarget(target) && ((ShulkerEntityAccessor) this.owner).requiem$getPeekAmount() > 50;
    }

    @Override
    public boolean run(LivingEntity target) {
        if (!this.owner.world.isClient) {
            this.owner.world.spawnEntity(new ShulkerBulletEntity(this.owner.world, this.owner, target, this.owner.getAttachedFace().getAxis()));
            this.owner.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (this.owner.world.random.nextFloat() - this.owner.world.random.nextFloat()) * 0.2F + 1.0F);
            this.beginCooldown();
        }

        return true;
    }
}
