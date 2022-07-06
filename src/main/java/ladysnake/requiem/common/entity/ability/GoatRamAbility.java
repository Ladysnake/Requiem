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
import ladysnake.requiem.core.entity.ability.MeleeAbility;
import ladysnake.requiem.mixin.common.access.BrainAccessor;
import ladysnake.requiem.mixin.common.access.PrepareRamTaskAccessor;
import ladysnake.requiem.mixin.common.access.RamImpactTaskAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.RamImpactTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class GoatRamAbility<O extends PathAwareEntity> extends DirectAbilityBase<O, Entity> {
    private static final long RAM_DURATION = 40;
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("2f3f65e2-a322-4a33-8d04-db7187941025");

    public static <O extends PathAwareEntity> GoatRamAbility<O> create(O owner) {
        @SuppressWarnings("unchecked") BrainAccessor<O> brain = (BrainAccessor<O>) owner.getBrain();
        RamImpactTaskAccessor<? super O> ramImpactTask = null;
        Function<? super O, SoundEvent> ramSoundFactory = null;
        ToIntFunction<? super O> cooldownFactory = null;
        float speed = 0;
        int maxRange = 0;

        for (var tasks : brain.getTasks().values()) {
            Set<Task<? super O>> ramActivity = tasks.get(Activity.RAM);
            if (ramActivity != null) {
                for (Task<? super O> task : ramActivity) {
                    if (task instanceof PrepareRamTaskAccessor<?>) {
                        @SuppressWarnings("unchecked") PrepareRamTaskAccessor<? super O> prepareRamTask = (PrepareRamTaskAccessor<? super O>) task;
                        ramSoundFactory = prepareRamTask.getSoundFactory();
                        cooldownFactory = prepareRamTask.getCooldownFactory();
                        speed = prepareRamTask.getSpeed();
                        maxRange = prepareRamTask.getMaxRamDistance();
                    } else if (task instanceof RamImpactTask) {
                        @SuppressWarnings("unchecked") var impactTask = (RamImpactTaskAccessor<? super O>) task;
                        ramImpactTask = impactTask;
                    }
                }
            }
        }

        if (ramImpactTask == null || ramSoundFactory == null) {
            throw new IllegalArgumentException(owner + " is not fit for ramming");
        }

        return new GoatRamAbility<>(
            owner,
            cooldownFactory,
            ramSoundFactory,
            ramImpactTask.getSoundFactory(),
            ramImpactTask.getStrengthMultiplierFactory(),
            ramImpactTask.getTargetPredicate(),
            speed,
            maxRange
        );
    }

    private final MeleeAbility backup;
    private final ToIntFunction<? super O> cooldownFactory;
    private final Function<? super O, SoundEvent> prepareRamSoundFactory;
    private final Function<? super O, SoundEvent> impactRamSoundFactory;
    private final ToDoubleFunction<? super O> strengthMultiplierFactory;
    private final TargetPredicate targetPredicate;
    private final float speed;
    private long ramStartTime;
    private boolean started;
    private Vec3d direction = Vec3d.ZERO;
    private Vec3d target = Vec3d.ZERO;

    protected GoatRamAbility(O owner, ToIntFunction<? super O> cooldownFactory, Function<? super O, SoundEvent> prepareRamSoundFactory, Function<? super O, SoundEvent> impactRamSoundFactory, ToDoubleFunction<? super O> strengthMultiplierFactory, TargetPredicate targetPredicate, float speed, int maxRange) {
        super(owner, cooldownFactory.applyAsInt(owner), maxRange, Entity.class);
        this.backup = new MeleeAbility(owner, false);
        this.cooldownFactory = cooldownFactory;
        this.prepareRamSoundFactory = prepareRamSoundFactory;
        this.impactRamSoundFactory = impactRamSoundFactory;
        this.strengthMultiplierFactory = strengthMultiplierFactory;
        this.targetPredicate = targetPredicate;
        this.speed = speed;
    }

    @Override
    public int getCooldownTime() {
        return this.cooldownFactory.applyAsInt(this.owner);
    }

    @Override
    public boolean canTarget(Entity target) {
        // Clientside, we only want to scan for targets when ramming is available
        return (!this.owner.world.isClient || this.owner.isSprinting()) && super.canTarget(target);
    }

    @Override
    public ActionResult trigger(Entity target) {
        if (super.trigger(target).isAccepted()) return ActionResult.CONSUME;
        return this.backup.trigger(target);
    }

    @Override
    protected boolean run(Entity target) {
        if (target instanceof LivingEntity && this.owner.isSprinting()) {
            if (!this.owner.world.isClient) {
                ServerWorld world = (ServerWorld) this.owner.world;
                long time = world.getTime();
                BlockPos startPos = this.owner.getBlockPos();
                EntityAttributeInstance speedAttribute = Objects.requireNonNull(this.owner.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
                // see RequiemCore#INHERENT_MOB_SLOWNESS for why speed * 3
                speedAttribute.addTemporaryModifier(
                    new EntityAttributeModifier(SPEED_MODIFIER_UUID, "Ram speed bonus", this.speed * 3, EntityAttributeModifier.Operation.MULTIPLY_BASE));
                this.target = target.getPos();
                this.ramStartTime = time;
                this.direction = (new Vec3d(startPos.getX() - target.getX(), 0.0, startPos.getZ() - target.getZ())).normalize();
                this.started = true;
                world.sendEntityStatus(this.owner, EntityStatuses.PREPARE_RAM);
                world.playSoundFromEntity(null, this.owner, this.prepareRamSoundFactory.apply(this.owner), SoundCategory.HOSTILE, 1.0F, this.owner.getSoundPitch());
            }
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();

        if (this.started) {
            ServerWorld world = (ServerWorld) this.owner.world;
            long time = world.getTime();

            if (this.owner.isSprinting() && time - this.ramStartTime < RAM_DURATION) {
                List<LivingEntity> list = world.getTargets(LivingEntity.class, this.targetPredicate, this.owner, this.owner.getBoundingBox());
                if (!list.isEmpty()) {
                    LivingEntity livingEntity = list.get(0);
                    livingEntity.damage(DamageSource.mob(this.owner).setNeutral(), (float)this.owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                    int speedAmount = this.owner.hasStatusEffect(StatusEffects.SPEED) ? this.owner.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
                    int slownessAmount = this.owner.hasStatusEffect(StatusEffects.SLOWNESS) ? this.owner.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1 : 0;
                    float multiplier = 0.25F * (float)(speedAmount - slownessAmount);
                    // arbitrary constant empirically chosen to roughly replicate a natural goat's knockback strength
                    float knockbackSpeed = (float) MathHelper.clamp(owner.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.4, 0.2F, 3.0F) + multiplier;
                    float knockbackModifier = livingEntity.blockedByShield(DamageSource.mob(this.owner)) ? 0.5F : 1.0F;
                    livingEntity.takeKnockback((double)(knockbackModifier * knockbackSpeed) * this.strengthMultiplierFactory.applyAsDouble(this.owner), this.direction.getX(), this.direction.getZ());
                    world.playSoundFromEntity(null, this.owner, this.impactRamSoundFactory.apply(this.owner), SoundCategory.HOSTILE, 1.0F, 1.0F);
                    this.finishRam();
                } else if (this.owner.getPos().distanceTo(this.target) < 0.25) {
                    this.finishRam();
                }
            } else {
                this.finishRam();
            }
        }
    }

    private void finishRam() {
        this.owner.world.sendEntityStatus(this.owner, EntityStatuses.FINISH_RAM);
        Objects.requireNonNull(this.owner.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).removeModifier(SPEED_MODIFIER_UUID);
        this.started = false;
    }
}
