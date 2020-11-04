/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class AttritionStatusEffect extends StatusEffect {
    public static final Identifier ATTRITION_BACKGROUND = Requiem.id("textures/gui/attrition_background.png");
    public static final DamageSource ATTRITION_HARDCORE_DEATH = new DamageSource("requiem.attrition.hardcore") {{
        // We need this dirty anonymous initializer because everything is protected
        this.setBypassesArmor();
        this.setOutOfWorld();
    }};

    public static void apply(PlayerEntity target) {
        StatusEffectInstance attrition = target.getStatusEffect(RequiemStatusEffects.ATTRITION);
        int amplifier = attrition == null ? 0 : attrition.getAmplifier() + 1;
        if (amplifier <= 3) {
            target.addStatusEffect(new StatusEffectInstance(
                RequiemStatusEffects.ATTRITION,
                300,
                amplifier,
                false,
                false,
                true
            ));
        } else {
            if (target.world.getLevelProperties().isHardcore()) {
                RemnantComponent.get(target).become(RemnantTypes.MORTAL);
                target.damage(ATTRITION_HARDCORE_DEATH, Float.MAX_VALUE);
            }
        }
    }

    public AttritionStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public double adjustModifierAmount(int amplifier, EntityAttributeModifier entityAttributeModifier) {
        return super.adjustModifierAmount(Math.min(amplifier, 3), entityAttributeModifier);
    }
}
