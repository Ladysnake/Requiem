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

import ladysnake.pandemonium.common.util.RayHelper;
import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class BlinkAbility extends IndirectAbilityBase<MobEntity> {
    public static final int COOLDOWN = 400;

    public BlinkAbility(MobEntity owner) {
        super(owner, COOLDOWN);
    }

    @Override
    public boolean trigger() {
        if (!this.owner.world.isClient) {
            Vec3d blinkPos = RayHelper.findBlinkPos(this.owner, 1F, 32D);
            if (this.owner.teleport(blinkPos.x, blinkPos.y, blinkPos.z, true)) {
                this.owner.world.playSound(null, this.owner.prevX, this.owner.prevY, this.owner.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.owner.getSoundCategory(), 1.0F, 1.0F);
                this.owner.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
        return true;
    }
}
