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
package ladysnake.pandemonium.common.entity;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import ladysnake.pandemonium.common.entity.chaos.AiAbyss;
import ladysnake.requiem.common.entity.RequiemTrackedDataHandlers;
import ladysnake.requiem.common.item.FilledSoulVesselItem;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.item.SoulFragmentInfo;
import ladysnake.requiem.core.mixin.access.MobEntityAccessor;
import ladysnake.requiem.mixin.common.access.BrainAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO make a SoulHolderComponent impl that ties isSoulless to hostedSoul
public class RunestoneGolemEntity extends TameableEntity {
    public static final TrackedData<Optional<EntityType<?>>> HOSTED_SOUL_TYPE = DataTracker.registerData(RunestoneGolemEntity.class, RequiemTrackedDataHandlers.OPTIONAL_ENTITY_TYPE);

    private @Nullable SoulFragmentInfo hostedSoul;
    private List<Activity> possibleActivities = List.of();

    public RunestoneGolemEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createRunestoneGolemAttributes() {
        return createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25F)
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
    }

    @CheckEnv(Env.SERVER)
    public void setHostedSoul(@Nullable SoulFragmentInfo soulFragment) {
        this.hostedSoul = soulFragment;
        Optional<EntityType<?>> entityType = Optional.ofNullable(soulFragment).flatMap(SoulFragmentInfo::entityType);
        if (entityType != this.getHostedSoulType()) this.clearAi();
        this.getDataTracker().set(HOSTED_SOUL_TYPE, entityType);
        entityType.ifPresent(this::setupAi);
    }

    @CheckEnv(Env.SERVER)
    public Optional<SoulFragmentInfo> getHostedSoul() {
        return Optional.ofNullable(this.hostedSoul);
    }

    public Optional<EntityType<?>> getHostedSoulType() {
        return this.getDataTracker().get(HOSTED_SOUL_TYPE);
    }

    private void clearAi() {
        Preconditions.checkState(!this.world.isClient);
        for (PrioritizedGoal pg : new HashSet<>(this.targetSelector.getGoals())) {
            this.targetSelector.remove(pg.getGoal());
        }
        for (PrioritizedGoal pg : new HashSet<>(this.goalSelector.getGoals())) {
            this.goalSelector.remove(pg.getGoal());
        }
        this.getBrain().stopAllTasks((ServerWorld) this.world, this);
        this.brain = this.deserializeBrain(new Dynamic<>(NbtOps.INSTANCE));
    }

    private void setupAi(EntityType<?> entityType) {
        Entity e = entityType.create(this.world);
        if (!(e instanceof MobEntity mob)) return;
        copyGoals(mob, ((MobEntityAccessor) mob).getTargetSelector(), this.targetSelector);
        copyGoals(mob, ((MobEntityAccessor) mob).getGoalSelector(), this.goalSelector);
        copyBrain(mob);
    }

    private void copyBrain(MobEntity mob) {
        this.brain = mob.getBrain().copy();

        @SuppressWarnings("unchecked") BrainAccessor<LivingEntity> theirBrain = (BrainAccessor<LivingEntity>) mob.getBrain();
        @SuppressWarnings("unchecked") BrainAccessor<RunestoneGolemEntity> ourBrain = (BrainAccessor<RunestoneGolemEntity>) this.getBrain();

        ourBrain.getSensors().entrySet().removeIf(e -> AiAbyss.isInvalidSensor(e.getValue(), this));
        this.getBrain().setSchedule(mob.getBrain().getSchedule());
        Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleState>>> requiredActivityMemories = theirBrain.getRequiredActivityMemories();
        // very approximate, but activities added first to the registry seem to have the least priority
        this.possibleActivities = List.copyOf(Lists.reverse(Registry.ACTIVITY.stream().filter(requiredActivityMemories::containsKey).toList()));
        ourBrain.getRequiredActivityMemories().putAll(requiredActivityMemories);
        ourBrain.getForgettingActivityMemories().putAll(theirBrain.getForgettingActivityMemories());
        this.getBrain().setCoreActivities(theirBrain.getCoreActivities());
        this.getBrain().setDefaultActivity(theirBrain.getDefaultActivity());

        for (Map.Entry<Integer, Map<Activity, Set<Task<? super LivingEntity>>>> prioritizedEntry : theirBrain.getTasks().entrySet()) {
            Map<Activity, Set<Task<? super RunestoneGolemEntity>>> newActivityMap = new HashMap<>();

            for (Map.Entry<Activity, Set<Task<? super LivingEntity>>> activityEntry : prioritizedEntry.getValue().entrySet()) {
                Set<Task<? super RunestoneGolemEntity>> newTaskSet = new LinkedHashSet<>();

                for (Task<? super LivingEntity> task : activityEntry.getValue()) {
                    AiAbyss.attune(task, mob, this).ifPresent(newTaskSet::add);
                }

                newActivityMap.put(activityEntry.getKey(), newTaskSet);
            }

            ourBrain.getTasks().put(prioritizedEntry.getKey(), newActivityMap);
        }

            this.getBrain().refreshActivities(this.world.getTimeOfDay(), this.world.getTime());
    }

    private void copyGoals(MobEntity mob, GoalSelector sourceGoalContainer, GoalSelector targetGoalContainer) {
        for (PrioritizedGoal goal : sourceGoalContainer.getGoals()) {
            AiAbyss.attune(goal.getGoal(), mob, this)
                .ifPresent(g -> targetGoalContainer.add(goal.getPriority(), g));
        }
    }

    @Override
    public Brain<RunestoneGolemEntity> getBrain() {
        @SuppressWarnings("unchecked") Brain<RunestoneGolemEntity> r = (Brain<RunestoneGolemEntity>) super.getBrain();
        return r;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(HOSTED_SOUL_TYPE, Optional.empty());
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return PandemoniumEntities.RUNESTONE_GOLEM.create(world);
    }

    @Override
    public ActionResult interactMob(PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (stack.isOf(RequiemItems.FILLED_SOUL_VESSEL)) {
            if (this.world.isClient) {
                return ActionResult.CONSUME;
            }

            Optional<SoulFragmentInfo> soul = FilledSoulVesselItem.parseSoulFragment(stack);

            if (soul.isPresent()) {
                this.setHostedSoul(soul.get());
                user.setStackInHand(hand, ItemUsage.exchangeStack(
                    stack,
                    user,
                    ((FilledSoulVesselItem) stack.getItem()).getEmptiedStack())
                );
                return ActionResult.SUCCESS;
            }
        } else if (stack.isOf(RequiemItems.EMPTY_SOUL_VESSEL)) {
            if (this.getHostedSoulType().isEmpty()) {   // available clientside
                return ActionResult.FAIL;
            }
            if (!this.world.isClient) {
                SoulFragmentInfo soul = this.getHostedSoul().orElseThrow(IllegalStateException::new);
                user.setStackInHand(hand, ItemUsage.exchangeStack(stack, user, FilledSoulVesselItem.forSoul(soul)));
                this.setHostedSoul(null);
            }
            return ActionResult.SUCCESS;
        }

        return super.interactMob(user, hand);
    }

    @Override
    protected void mobTick() {
        if (!this.possibleActivities.isEmpty()) {
            this.world.getProfiler().push("runestoneGolemBrain");
            this.getBrain().tick((ServerWorld)this.world, this);
            this.world.getProfiler().pop();

            if (this.getBrain().getSchedule() == Schedule.EMPTY) {
                this.world.getProfiler().push("runestoneGolemActivityUpdate");
                this.tickActivities();
                this.world.getProfiler().pop();
            }
        }
        super.mobTick();
    }

    private void tickActivities() {
        this.getBrain().resetPossibleActivities(this.possibleActivities);
        this.setAttacking(this.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.hostedSoul != null) {
            nbt.put("soul_fragment", this.hostedSoul.toNbt());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setHostedSoul(SoulFragmentInfo.fromNbt(nbt.getCompound("soul_fragment")).orElse(null));
    }
}
