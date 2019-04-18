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
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MeleeAbility extends DirectAbilityBase<MobEntity> {
    private final boolean ignoreDamageAttribute;

    public MeleeAbility(MobEntity owner) {
        this(owner, false);
    }

    public MeleeAbility(MobEntity owner, boolean ignoreDamageAttribute) {
        super(owner);
        this.ignoreDamageAttribute = ignoreDamageAttribute;
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity target) {
        // We actually need to check if the entity has an attack damage attribute, because mojang doesn't.
        boolean success = (ignoreDamageAttribute || owner.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) && owner.attack(target);
        if (success && target instanceof LivingEntity) {
            player.getMainHandStack().onEntityDamaged((LivingEntity) target, player);
        }
        return success;
    }
}
