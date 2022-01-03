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
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AxolotlEntity;

import java.util.Objects;
import java.util.UUID;

public class AxolotlPlayingDeadAbility extends IndirectAbilityBase<AxolotlEntity> {
    public static final int COOLDOWN = 20 * 30;
    public static final int ABILITY_TIME = AxolotlEntity.PLAY_DEAD_TICKS;
    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("5a13c664-7932-4a73-b341-0745cb737754");
    public static final int MAX_HP_HEALED = 4;

    private int ticksLeft;

    public AxolotlPlayingDeadAbility(AxolotlEntity owner) {
        super(owner, COOLDOWN);
    }

    @Override
    protected boolean run() {
        int missingHealth = (int) (this.owner.getMaxHealth() - this.owner.getHealth());
        if (missingHealth > 0) {
            this.ticksLeft = Math.round(ABILITY_TIME / Math.min(1F, (float) missingHealth / MAX_HP_HEALED));
            EntityAttributeInstance speedAttr = Objects.requireNonNull(this.owner.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            speedAttr.removeModifier(SPEED_MODIFIER_UUID);
            speedAttr.addTemporaryModifier(new EntityAttributeModifier(SPEED_MODIFIER_UUID, "playing dead slowdow", -0.9, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            this.owner.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, ticksLeft));
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (this.ticksLeft > 0) {
            this.ticksLeft--;
            this.owner.setPlayingDead(true);

            if (this.ticksLeft == 0) {
                Objects.requireNonNull(this.owner.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).removeModifier(SPEED_MODIFIER_UUID);
            }
        }
    }
}
