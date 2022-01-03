/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.util;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.ai.TargetPredicate;

/**
 * Interface implemented by {@link TargetPredicate} when Requiem is installed.
 */
public interface RequiemTargetPredicate {
    static TargetPredicate includeIncorporeal(TargetPredicate predicate) {
        ((RequiemTargetPredicate) predicate).requiem$includeIncorporeal();
        return predicate;
    }

    /**
     * Causes this target predicate to include players that are {@link RemnantComponent#isIncorporeal() incorporeal}.
     */
    void requiem$includeIncorporeal();
}
