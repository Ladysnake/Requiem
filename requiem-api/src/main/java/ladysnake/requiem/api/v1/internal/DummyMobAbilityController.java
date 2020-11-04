/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
import net.minecraft.nbt.CompoundTag;

public class DummyMobAbilityController implements MobAbilityController {
    public static final MobAbilityController INSTANCE = new DummyMobAbilityController();

    @Override
    public double getRange(AbilityType type) {
        return 0;
    }

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

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        // NO-OP
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        // NO-OP
    }
}
