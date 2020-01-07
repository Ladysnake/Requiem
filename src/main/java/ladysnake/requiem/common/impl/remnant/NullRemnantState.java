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
package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NullRemnantState implements RemnantState {

    public static final RemnantState NULL_STATE = new NullRemnantState();

    @Override
    public boolean isIncorporeal() {
        return false;
    }

    @Override
    public boolean isSoul() {
        return false;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        // NO-OP
    }

    @Override
    public RemnantType getType() {
        return RemnantTypes.MORTAL;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void copyFrom(ServerPlayerEntity original, boolean lossless) {
        // NO-OP
    }
}
