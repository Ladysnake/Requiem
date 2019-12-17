/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
package ladysnake.requiem.api.v1.internal;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;

public class DummyMobAbilityController implements MobAbilityController {

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        return false;
    }

    @Override
    public void updateAbilities() {
        // NO-OP
    }
}
