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
package ladysnake.requiem.core.util;

import ladysnake.requiem.api.v1.event.requiem.HumanityCheckCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class DamageHelper {
    public static int getHumanityLevel(DamageSource source) {
        LivingEntity attacker = getPossessionAttacker(source);
        if (attacker != null) {
            return HumanityCheckCallback.EVENT.invoker().getHumanityLevel(attacker);
        }
        return 0;
    }

    /**
     * If applicable, returns the possessed entity that is responsible for the damage
     */
    @Nullable
    private static LivingEntity getPossessionAttacker(DamageSource source) {
        // players do not care about humanity anyway, so we check for their possessed entity directly
        Entity attacker = source.getAttacker() instanceof PlayerEntity ? PossessionComponent.getHost(source.getAttacker()) : source.getAttacker();

        // check that the attacker is being possessed, and that it can use its equipment
        if (attacker instanceof Possessable && ((Possessable) attacker).isBeingPossessed() && attacker.getType().isIn(RequiemCoreTags.Entity.ITEM_USERS)) {
            return (LivingEntity) attacker;
        }

        return null;
    }

    public static DamageSource tryProxyDamage(DamageSource source, LivingEntity attacker) {
        Entity delegate = PossessionComponent.getHost(attacker);
        return delegate != null ? createProxiedDamage(source, delegate) : null;
    }

    @Nullable
    public static DamageSource createProxiedDamage(DamageSource source, Entity newAttacker) {
        if (source instanceof ProjectileDamageSource) {
            return new ProjectileDamageSource(source.getName(), source.getSource(), newAttacker);
        } else if (source instanceof EntityDamageSource) {
            return new EntityDamageSource(source.getName(), newAttacker);
        }
        return null;
    }
}
