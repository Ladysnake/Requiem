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
package ladysnake.requiem.common.advancement.criterion;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.mixin.registration.CriteriaAccessor;
import net.minecraft.advancement.criterion.Criteria;

public class RequiemCriteria {
    public static final OnResurrectCriterion PLAYER_RESURRECTED_AS_ENTITY = new OnResurrectCriterion(Requiem.id("player_resurrected_as_entity"));
    public static final OnRemnantChoiceCriterion MADE_REMNANT_CHOICE = new OnRemnantChoiceCriterion(Requiem.id("made_remnant_choice"));

    public static void init() {
        // the class may not have been loaded at this point, so we need to classload it ourselves
        // before calling the accessor
        Criteria.getCriteria();
        CriteriaAccessor.invokeRegister(PLAYER_RESURRECTED_AS_ENTITY);
        CriteriaAccessor.invokeRegister(MADE_REMNANT_CHOICE);
    }
}
