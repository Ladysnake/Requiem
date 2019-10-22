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
package ladysnake.requiem.common.entity.ai.attribute;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class CooldownStrengthAttribute extends DelegatingAttribute {
    private final Possessable owner;

    public <T extends LivingEntity & Possessable> CooldownStrengthAttribute(T entity) {
        super(entity.getAttributes(), entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE));
        this.owner = entity;
    }

    @Override
    public double getValue() {
        final double strength = super.getValue();
        PlayerEntity possessor = this.owner.getPossessor();
        if (possessor != null) {
            double attackCharge = possessor.getAttackCooldownProgress(0.5f);
            return strength * (0.2F + attackCharge * attackCharge * 0.8F);
        }
        return strength;
    }
}
