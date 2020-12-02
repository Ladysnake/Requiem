/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.mob.CreeperEntity;

public class CreeperPrimingAbility extends IndirectAbilityBase<CreeperEntity> {
    public CreeperPrimingAbility(CreeperEntity owner) {
        super(owner, 0);
    }

    @Override
    public Result trigger() {
        if (!this.owner.world.isClient) {
            this.owner.setFuseSpeed(this.owner.getFuseSpeed() > 0 ? -1 : 1);
        }
        return Result.SUCCESS;
    }
}
