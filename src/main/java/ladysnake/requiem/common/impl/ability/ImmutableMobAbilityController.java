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
package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.*;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

public class ImmutableMobAbilityController<T extends MobEntity & Possessable> implements MobAbilityController {
    private final IndirectAbility<? super T> indirectAttack;
    private final IndirectAbility<? super T> indirectInteraction;
    private final DirectAbility<? super T, ?> directAttack;
    private final DirectAbility<? super T, ?> directInteraction;
    private final T owner;

    public ImmutableMobAbilityController(MobAbilityConfig<? super T> config, T owner) {
        this.owner = owner;
        this.directAttack = config.getDirectAbility(owner, AbilityType.ATTACK);
        this.directInteraction = config.getDirectAbility(owner, AbilityType.INTERACT);
        this.indirectAttack = config.getIndirectAbility(owner, AbilityType.ATTACK);
        this.indirectInteraction = config.getIndirectAbility(owner, AbilityType.INTERACT);
    }

    @Override
    public double getRange(AbilityType type) {
        return this.getDirect(type).getRange();
    }

    @Override
    public boolean canTarget(AbilityType type, Entity target) {
        return this.canTarget(target, this.getDirect(type));
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        return this.use(this.owner.getPossessor(), this.getDirect(type));
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        return this.getIndirect(type).trigger();
    }

    @Override
    public void tick() {
        this.directAttack.update();
        this.indirectAttack.update();
        this.directInteraction.update();
        this.indirectInteraction.update();
    }

    private DirectAbility<? super T, ?> getDirect(AbilityType type) {
        switch (type) {
            case ATTACK:
                return this.directAttack;
            case INTERACT:
                return this.directInteraction;
        }
        throw new IllegalArgumentException("Unrecognized ability type " + type);
    }

    private IndirectAbility<? super T> getIndirect(AbilityType type) {
        switch (type) {
            case ATTACK:
                return this.indirectAttack;
            case INTERACT:
                return this.indirectInteraction;
        }
        throw new IllegalArgumentException("Unrecognized ability type " + type);
    }

    private <E extends Entity> boolean canTarget(Entity target, DirectAbility<? super T, E> ability) {
        Class<E> targetType = ability.getTargetType();
        if (targetType.isInstance(target)) {
            return ability.canTrigger(targetType.cast(target));
        }
        return false;
    }

    private <E extends Entity> boolean use(@Nullable PlayerEntity p, DirectAbility<? super T, E> ability) {
        if (ability.getTargetType().isInstance(p)) {
            return ability.trigger(ability.getTargetType().cast(p));
        }
        return false;
    }
}
