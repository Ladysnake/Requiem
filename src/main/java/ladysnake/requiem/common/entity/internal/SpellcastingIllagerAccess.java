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
package ladysnake.requiem.common.entity.internal;

import ladysnake.requiem.core.util.reflection.ReflectionHelper;
import ladysnake.requiem.core.util.reflection.UnableToFindMethodException;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public abstract class SpellcastingIllagerAccess extends SpellcastingIllagerEntity {
    public static final SpellcastingIllagerEntity.Spell SPELL_NONE = Spell.NONE;
    public static final Spell SPELL_FANGS = Spell.FANGS;
    public static final Method CAST_SPELL_GOAL$CAST_SPELL;

    static {
        try {
            CAST_SPELL_GOAL$CAST_SPELL = ReflectionHelper.findMethodFromIntermediary(CastSpellGoal.class, "method_7148", void.class);
        } catch (UnableToFindMethodException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    private SpellcastingIllagerAccess(EntityType<? extends SpellcastingIllagerEntity> entityType, World world) {
        super(entityType, world);
    }
}
