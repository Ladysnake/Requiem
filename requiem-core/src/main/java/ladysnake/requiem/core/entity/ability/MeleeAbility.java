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
package ladysnake.requiem.core.entity.ability;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MeleeAbility extends DirectAbilityBase<MobEntity, Entity> {
    private final boolean ignoreDamageAttribute;

    public MeleeAbility(MobEntity owner) {
        this(owner, false);
    }

    public MeleeAbility(MobEntity owner, boolean ignoreDamageAttribute) {
        super(owner, 0, 0, Entity.class);
        this.ignoreDamageAttribute = ignoreDamageAttribute;
    }

    @Override
    public boolean run(Entity target) {
        // We actually need to check if the entity has an attack damage attribute, because mojang doesn't.
        if (!ignoreDamageAttribute && owner.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) == null) {
            return false;
        }

        if (this.owner.world.isClient) return true;

        boolean success = owner.tryAttack(target);

        if (success && target instanceof LivingEntity) {
            this.owner.setAttacking(true);
            PlayerEntity player = ((Possessable) this.owner).getPossessor();

            if (player != null) {
                player.getMainHandStack().postHit((LivingEntity) target, player);
                // Reset cooldown
                player.resetLastAttackedTicks();
            }
        }
        return success;
    }
}
