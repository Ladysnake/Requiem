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
package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.*;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ImmutableMobAbilityController<T extends MobEntity & Possessable> implements MobAbilityController {
    private final IndirectAbility<? super T> indirectAttack;
    private final IndirectAbility<? super T> indirectInteraction;
    private final DirectAbility<? super T> directAttack;
    private final DirectAbility<? super T> directInteraction;
    private final T owner;

    public ImmutableMobAbilityController(MobAbilityConfig<? super T> config, T owner) {
        this.owner = owner;
        this.directAttack = config.getDirectAbility(owner, AbilityType.ATTACK);
        this.directInteraction = config.getDirectAbility(owner, AbilityType.INTERACT);
        this.indirectAttack = config.getIndirectAbility(owner, AbilityType.ATTACK);
        this.indirectInteraction = config.getIndirectAbility(owner, AbilityType.INTERACT);
    }

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        PlayerEntity p = this.owner.getPossessor();
        if (type == AbilityType.ATTACK) {
            return p != null && directAttack.trigger(p, target);
        } else if (type == AbilityType.INTERACT) {
            return p != null && directInteraction.trigger(p, target);
        }
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        PlayerEntity p = this.owner.getPossessor();
        if (type == AbilityType.ATTACK) {
            return p != null && indirectAttack.trigger(p);
        } else if (type == AbilityType.INTERACT) {
            return p != null && indirectInteraction.trigger(p);
        }
        return false;
    }

    @Override
    public void updateAbilities() {
        if (!this.owner.world.isClient) {
            this.directAttack.update();
            this.indirectAttack.update();
            this.directInteraction.update();
            this.indirectInteraction.update();
        }
    }
}
