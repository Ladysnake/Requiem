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

import ladysnake.requiem.core.entity.ability.DirectAbilityBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Objects;
import java.util.Optional;

public class FrogCatchAbility extends DirectAbilityBase<FrogEntity, Entity> {
    private int eatAnimationTicks = -1;

    public FrogCatchAbility(FrogEntity owner) {
        super(owner, 20, 1.75, Entity.class);
    }

    @Override
    protected boolean run(Entity target) {
        FrogEntity frog = this.owner;

        if (frog.world.isClient) return true;
        // Taken from CatchAndEatTask#update
        frog.setTargetEntity(target);
        frog.world.playSoundFromEntity(null, frog, SoundEvents.ENTITY_FROG_TONGUE, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        frog.setPose(EntityPose.USING_TONGUE);
        target.setVelocity(target.getPos().relativize(frog.getPos()).normalize().multiply(0.75));
        this.eatAnimationTicks = 0;
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (!this.owner.world.isClient) {
            Optional<Entity> target = this.owner.getTargetEntity();

            if (target.isPresent()) {
                if (this.eatAnimationTicks >= 10) {
                    this.owner.clearTargetEntity();
                    this.owner.setPose(EntityPose.STANDING);
                    this.eatAnimationTicks = -1;
                } else if (this.eatAnimationTicks == 6) {
                    this.eatTargetEntity(this.owner, target.get());
                }
                this.eatAnimationTicks++;
            }
        }
    }

    /**
     * Copy of {@link net.minecraft.entity.ai.brain.task.CatchAndEatEntityTask#eatTargetEntity(ServerWorld, FrogEntity)},
     * tweaked to allow hitting other entities without being OP
     */
    private void eatTargetEntity(FrogEntity frog, Entity entity) {
        frog.world.playSoundFromEntity(null, frog, SoundEvents.ENTITY_FROG_EAT, SoundCategory.NEUTRAL, 2.0F, 1.0F);

        if (entity.isAlive()) {
            EntityAttributeInstance attackDamage = Objects.requireNonNull(frog.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE));
            double baseAttackValue = attackDamage.getBaseValue();

            if (!(entity instanceof LivingEntity living && FrogEntity.canEat(living))) {
                attackDamage.setBaseValue(1);
            }

            frog.tryAttack(entity);

            attackDamage.setBaseValue(baseAttackValue);

            if (!entity.isAlive()) {
                entity.remove(Entity.RemovalReason.KILLED);
            }
        }
    }
}
