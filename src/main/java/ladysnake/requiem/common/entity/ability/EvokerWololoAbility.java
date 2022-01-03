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

import ladysnake.requiem.common.entity.WololoComponent;
import ladysnake.requiem.common.entity.internal.SpellcastingIllagerAccess;
import ladysnake.requiem.core.entity.ability.DirectAbilityBase;
import ladysnake.requiem.mixin.common.possession.gameplay.ability.EvokerEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EvokerEntity;
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
        if (!super.canTarget(target)) return false;
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
                this.owner.setSpell(SpellcastingIllagerAccess.SPELL_NONE);
            }
        }
    }

    final class CustomWololoGoal extends EvokerEntity.WololoGoal implements ExtendedWololoGoal {
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
                ((EvokerEntityAccessor) EvokerWololoAbility.this.owner).requiem$invokeSetWololoTarget((SheepEntity) this.target);
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
