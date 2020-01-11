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
 */
package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public final class RequiemStatusEffects {
    public static final StatusEffect ATTRITION = new AttritionStatusEffect(StatusEffectType.HARMFUL, 0xAA3322)
        .addAttributeModifier(EntityAttributes.MAX_HEALTH, "069ae0b1-4014-41dd-932f-a5da4417d711", -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static void init() {
        registerEffect(ATTRITION, "attrition");
    }

    public static void registerEffect(StatusEffect effect, String name) {
        Registry.register(Registry.STATUS_EFFECT, Requiem.id(name), effect);
    }
}
