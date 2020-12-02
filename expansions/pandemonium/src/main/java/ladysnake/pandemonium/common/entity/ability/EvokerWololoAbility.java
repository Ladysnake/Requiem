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

import ladysnake.pandemonium.common.entity.WololoComponent;
import ladysnake.pandemonium.mixin.common.entity.mob.EvokerEntityAccessor;
import ladysnake.requiem.common.entity.ability.DirectAbilityBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.jetbrains.annotations.Nullable;

public class EvokerWololoAbility extends DirectAbilityBase<EvokerEntity, Entity> {
    private final CustomWololoGoal wololoGoal;
    private boolean started;

    public EvokerWololoAbility(EvokerEntity owner) {
        super(owner, 0, 16, Entity.class);
        this.wololoGoal = new CustomWololoGoal(owner);
    }

    @Override
    public boolean canTarget(Entity target) {
        if (target instanceof SheepEntity) {
            return this.wololoGoal.requiem_getConvertibleSheepPredicate().test(null, (SheepEntity) target);
        }
        return WololoComponent.canBeConverted(target);
    }

    @Override
    public boolean run(Entity target) {
        if (this.owner.world.isClient) return true;

        this.wololoGoal.target = target;

        if (this.wololoGoal.canStart()) {
            this.wololoGoal.start();
            this.beginCooldown();
            this.started = true;
            return true;
        } else {
            this.wololoGoal.target = null;
            return false;
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.started) {
            if (this.wololoGoal.shouldContinue()) {
                this.wololoGoal.tick();
            } else {
                this.started = false;
                this.wololoGoal.stop();
                this.owner.setSpell(SpellcastingIllagerEntity.Spell.NONE);
            }
        }
    }

    class CustomWololoGoal extends EvokerEntity.WololoGoal implements ExtendedWololoGoal {
        private @Nullable Entity target;

        private CustomWololoGoal(EvokerEntity evoker) {
            evoker.super();
        }

        @Override
        public boolean shouldContinue() {
            return this.requiem_hasValidTarget() && this.spellCooldown > 0;
        }

        @Override
        protected void castSpell() {
            assert this.target != null;
            if (this.target instanceof SheepEntity) {
                ((EvokerEntityAccessor) EvokerWololoAbility.this.owner).invokeSetWololoTarget((SheepEntity) this.target);
                super.castSpell();
            } else {
                WololoComponent.KEY.maybeGet(this.target).ifPresent(WololoComponent::wololo);
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.target = null;
        }

        @Override
        public boolean requiem_hasValidTarget() {
            return this.target != null && EvokerWololoAbility.this.canTarget(this.target);
        }
    }
}
