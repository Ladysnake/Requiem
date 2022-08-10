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
package ladysnake.pandemonium.common.entity.chaos;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import ladysnake.pandemonium.common.entity.RunestoneGolemEntity;
import ladysnake.pandemonium.common.entity.ai.brain.task.generic.GenerifiedGoToPointOfInterestTask;
import ladysnake.pandemonium.common.entity.ai.brain.task.generic.GenerifiedPanicTask;
import ladysnake.pandemonium.common.entity.ai.brain.task.generic.GenerifiedVillagerWalkTowardsTask;
import ladysnake.pandemonium.common.entity.ai.brain.task.generic.GenerifiedWalkTowardJobSiteTask;
import ladysnake.pandemonium.mixin.common.GoToPointOfInterestTaskAccessor;
import ladysnake.pandemonium.mixin.common.VillagerWalkTowardsTaskAccessor;
import ladysnake.pandemonium.mixin.common.WalkTowardsJobSiteTaskAccessor;
import ladysnake.pandemonium.mixin.common.WeightedListAccessor;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.PanicTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HoldInHandsGoal;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.util.collection.WeightedList;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AiAbyss {
    static final Unsafe abholos;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            abholos = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Failed to summon The Unsafe One", e);
        }
    }

    private static final ClassAnalyser<Goal> goalAnalyzer = new GoalClassAnalyser();
    private static final ClassAnalyser<Task<?>> taskAnalyser = new TaskClassAnalyser();

    public static Optional<Goal> attune(Goal goal, MobEntity currentHost, RunestoneGolemEntity newHost) {
        return dissectGoal(goal.getClass())
            .filter(goalTypeInfo -> goalTypeInfo.implClass() != null)
            .map(goalTypeInfo -> conceiveShadowClone(goal, goalTypeInfo, currentHost, newHost));
    }

    public static Optional<Task<? super RunestoneGolemEntity>> attune(Task<?> task, MobEntity currentHost, RunestoneGolemEntity newHost) {
        if (task instanceof PanicTask) {
            return Optional.of(new GenerifiedPanicTask<>());
        } else if (task instanceof WalkTowardsJobSiteTaskAccessor t) {
            return Optional.of(new GenerifiedWalkTowardJobSiteTask<>(t.getSpeed()));
        } else if (task instanceof VillagerWalkTowardsTaskAccessor t) {
            return Optional.of(new GenerifiedVillagerWalkTowardsTask(t.getDestination(), t.getSpeed(), t.getCompletionRange(), t.getMaxRange(), t.getMaxRunTime()));
        } else if (task instanceof GoToPointOfInterestTaskAccessor t) {
            return Optional.of(new GenerifiedGoToPointOfInterestTask(t.getSpeed(), t.getCompletionRange()));
        } else {
            @SuppressWarnings("unchecked") Class<? extends Task<?>> tc = (Class<? extends Task<?>>) task.getClass();
            return dissectTask(tc)
                .filter(taskTypeInfo -> taskTypeInfo.implClass() != null)
                .map(taskTypeInfo -> conceiveShadowClone(task, taskTypeInfo, currentHost, newHost));
        }
    }

    private static Optional<ClassInfo<Goal>> dissectGoal(Class<? extends Goal> gc) {
        return goalAnalyzer.dissect(gc);
    }

    private static Optional<ClassInfo<Task<?>>> dissectTask(Class<? extends Task<?>> tc) {
        return taskAnalyser.dissect(tc);
    }

    private static Goal conceiveShadowClone(Goal goal, ClassInfo<Goal> info, MobEntity currentHost, RunestoneGolemEntity newHost) {
        Preconditions.checkNotNull(info.implClass());
        Preconditions.checkState(Goal.class.isAssignableFrom(info.implClass()) && !Modifier.isAbstract(info.implClass().getModifiers()));

        try {
            Goal cursedChild = (Goal) abholos.allocateInstance(info.implClass());

            for (HexedField<?> field : info.fields()) {
                field.copy(goal, cursedChild, (value, f) -> value == currentHost ? f.cast(newHost) : value);
            }

            return cursedChild;
        } catch (InstantiationException e) {
            throw new UncheckedReflectionException("Failed to instantiate massively cursed goal", e);
        }
    }

    private static @Nullable Task<? super RunestoneGolemEntity> conceiveShadowClone(Task<?> task, ClassInfo<Task<?>> info, MobEntity currentHost, RunestoneGolemEntity newHost) {
        Preconditions.checkNotNull(info.implClass());
        Preconditions.checkState(Task.class.isAssignableFrom(info.implClass()) && !Modifier.isAbstract(info.implClass().getModifiers()));

        try {
            @SuppressWarnings("unchecked") Task<? super RunestoneGolemEntity> cursedChild = (Task<? super RunestoneGolemEntity>) abholos.allocateInstance(info.implClass());

            for (HexedField<?> field : info.fields()) {
                try {
                    field.copy(task, cursedChild, (value, f) -> {
                        if (value instanceof Task<?> t) {
                            return f.cast(attune(t, currentHost, newHost).orElseThrow(() -> new CloneFailedException("Could not convert task " + t)));
                        } else if (isTaskList(value, f)) {
                            @SuppressWarnings("unchecked") WeightedListAccessor<Task<?>> taskList = (WeightedListAccessor<Task<?>>) value;
                            WeightedList<Task<?>> converted = new WeightedList<>();
                            for (WeightedList.Entry<Task<?>> entry : taskList.getEntries()) {
                                attune(entry.getElement(), currentHost, newHost).ifPresent(t -> converted.add(t, entry.getWeight()));
                            }
                            return f.cast(converted);
                        } else {
                            // Tasks delegate most entity-specific matters to lambda functions, which need to be converted
                            Object lambdaReplacement = FunctionalRecycler.tryRepurpose(value, f.field().getGenericType(), newHost);
                            if (lambdaReplacement != null) {
                                return f.cast(lambdaReplacement);
                            }
                        }
                        return value;
                    });
                } catch (CloneFailedException e) {
                    Requiem.LOGGER.error("Failed to clone field {} in task {}: {}", field.field(), task, e.getMessage());
                    return null;    // yeet the child (leaving a half-initialized object to the garbage collector is fine right ?)
                }
            }

            return cursedChild;
        } catch (InstantiationException e) {
            throw new UncheckedReflectionException("Failed to instantiate massively cursed goal", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static boolean isTaskList(Object value, HexedField<?> f) {
        return value instanceof WeightedList<?> && TypeToken.of(((ParameterizedType) f.field().getGenericType()).getActualTypeArguments()[0]).getRawType() == Task.class;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean isInvalidSensor(Sensor<?> value, RunestoneGolemEntity runestoneGolemEntity) {
        TypeToken<?> parameterizedSensor = TypeToken.of(value.getClass()).getSupertype(Sensor.class);
        Type sensorArg = ((ParameterizedType) parameterizedSensor.getType()).getActualTypeArguments()[0];
        return isIncompatible(sensorArg, runestoneGolemEntity.getClass(), false);
    }

    private record ClassInfo<T>(@Nullable Class<? extends T> implClass, Set<HexedField<?>> fields) {
    }

    private abstract static class ClassAnalyser<T> {
        private final Map<Class<? extends T>, Optional<ClassInfo<T>>> cache = new IdentityHashMap<>();
        private final Class<T> upperBound;

        ClassAnalyser(Class<T> upperBound) {
            this.upperBound = upperBound;
        }

        public Optional<ClassInfo<T>> dissect(Class<? extends T> declaredType) {
            return this.cache.computeIfAbsent(declaredType, klass -> {
                @Nullable @SuppressWarnings("unchecked") Class<? extends T> superclass = klass == this.upperBound
                    ? null
                    : (Class<? extends T>) klass.getSuperclass();

                Optional<ClassInfo<T>> parentInfo = superclass == null ? Optional.empty() : this.dissect(superclass);

                // If the parent is supposed to be substituted, obviously this too
                if (parentInfo.isPresent() && parentInfo.get().implClass() != null && parentInfo.get().implClass() != superclass) {
                    return parentInfo;
                }

                if (this.shouldSubstituteWithParent(klass)) {
                    return parentInfo;
                }

                Set<HexedField<?>> fields = new HashSet<>(parentInfo.map(ClassInfo::fields).orElse(Set.of()));

                for (Field declaredField : klass.getDeclaredFields()) {
                    if (!Modifier.isStatic(declaredField.getModifiers())) {
                        declaredField.setAccessible(true);
                        fields.add(HexedField.hex(declaredField));
                    }
                }

                // Can't instantiate an abstract class, so we just keep field info for subtypes
                Class<? extends T> implClass = Modifier.isAbstract(klass.getModifiers()) ? null : klass;

                return Optional.of(new ClassInfo<>(implClass, fields));
            });
        }

        protected abstract boolean shouldSubstituteWithParent(Class<? extends T> klass);

    }

    private static class GoalClassAnalyser extends ClassAnalyser<Goal> {
        private static final List<Class<?>> entityRootClasses = List.of(Entity.class, RangedAttackMob.class);
        /**
         * Goal classes that are definitely unsafe for whatever reason
         */
        private static final Set<Class<? extends Goal>> unsafeGoalClasses = Set.of(
            HoldInHandsGoal.class   // uses an entity predicate task-style
        );
        /**
         * Those kinda just like unsafe casting
         */
        // oh ye the JDK unmodifiable sets kinda just hate nulls
        private static final Set<Class<? extends MobEntity>> unsafeEnclosingClasses = ImmutableSet.of(
            VindicatorEntity.class, DrownedEntity.class
        );

        public GoalClassAnalyser() {
            super(Goal.class);
        }

        @Override
        protected boolean shouldSubstituteWithParent(Class<? extends Goal> gc) {
            return isUnsafeGoal(gc);
        }

        private static boolean isUnsafeGoal(Class<? extends Goal> gc) {
            if (isDefinitelySafe(gc)) return false;
            if (isDefinitelyUnsafe(gc)) return true;

            for (Constructor<?> constructor : gc.getDeclaredConstructors()) {
                for (Parameter cntParam : constructor.getParameters()) {
                    // Assume that an entity passed to a constructor must be the goal's owner
                    // Even if not, it's probably attached to the owner in some way
                    for (Class<?> entityRootClass : entityRootClasses) {
                        if (entityRootClass.isAssignableFrom(cntParam.getType())) {
                            if (!cntParam.getType().isAssignableFrom(RunestoneGolemEntity.class)) {
                                // This goal is expecting an owner incompatible with our golems,
                                // let's see if there is something usable higher up
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private static boolean isDefinitelySafe(Class<? extends Goal> goalClass) {
            // Spiders' attack goals are actually fine with any entity
            return goalClass.getEnclosingClass() == SpiderEntity.class;
        }

        private static boolean isDefinitelyUnsafe(Class<? extends Goal> goalClass) {
            return unsafeGoalClasses.contains(goalClass) || unsafeEnclosingClasses.contains(goalClass.getEnclosingClass());
        }
    }

    private static class TaskClassAnalyser extends ClassAnalyser<Task<?>> {
        @SuppressWarnings("unchecked")
        TaskClassAnalyser() {
            super((Class<Task<?>>) (Class<?>) Task.class);
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        protected boolean shouldSubstituteWithParent(Class<? extends Task<?>> tc) {
            // Task<E extends LivingEntity>
            ParameterizedType parameterizedTaskType = (ParameterizedType) TypeToken.of(tc).getSupertype(Task.class).getType();
            // <E extends LivingEntity>
            Type ownerType = parameterizedTaskType.getActualTypeArguments()[0];
            return isIncompatible(ownerType, RunestoneGolemEntity.class, false);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    static boolean isIncompatible(Type type1, Class<? extends LivingEntity> type2, boolean reverse) {
        if (type1 instanceof TypeVariable<?> variable) {
            // e.g. ownerType = <E extends ...>
            for (Type upperBound : variable.getBounds()) {
                if (isIncompatible(upperBound, type2, reverse)) return true;
            }
            return false;
        } else if (type1 instanceof Class<?> cls) {
            return reverse ? !type2.isAssignableFrom(cls) : !cls.isAssignableFrom(type2);
        } else {
            // Unlikely to get here, but at least we can more or less handle anything
            return reverse ? !TypeToken.of(type2).isSupertypeOf(type1) : !TypeToken.of(type1).isSupertypeOf(type2);
        }
    }
}
