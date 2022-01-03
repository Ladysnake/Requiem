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
package ladysnake.requiem.api.v1.entity.ability;

public enum AbilityType {
    /**
     * {@code ATTACK} abilities are triggered by players attempting to hit something.
     * They usually result in entity/terrain damage.
     */
    ATTACK,
    /**
     * {@code INTERACT} abilities are triggered by players attempting to interact with something.
     * Their effects are broader than {@link #ATTACK}, ranging from spellcasting to teleporting.
     */
    INTERACT
}
