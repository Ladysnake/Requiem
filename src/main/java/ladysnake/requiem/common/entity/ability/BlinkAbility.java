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
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.core.entity.ability.IndirectAbilityBase;
import ladysnake.requiem.core.util.RayHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class BlinkAbility extends IndirectAbilityBase<LivingEntity> {
    public static final int COOLDOWN = 400;

    public BlinkAbility(LivingEntity owner) {
        super(owner, COOLDOWN);
    }

    @Override
    public boolean run() {
        if (!this.owner.world.isClient) {
            Vec3d blinkPos = RayHelper.findBlinkPos(this.owner, 1F, 32D);
            if (this.owner.teleport(blinkPos.x, blinkPos.y, blinkPos.z, true)) {
                this.owner.world.playSound(null, this.owner.prevX, this.owner.prevY, this.owner.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.owner.getSoundCategory(), 1.0F, 1.0F);
                this.owner.world.playSound(null, blinkPos.x, blinkPos.y, blinkPos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.owner.getSoundCategory(), 1.0F, 1.0F);
            }
        }
        return true;
    }
}
