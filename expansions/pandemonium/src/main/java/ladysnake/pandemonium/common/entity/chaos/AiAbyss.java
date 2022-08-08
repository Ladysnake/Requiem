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
import com.google.common.reflect.TypeToken;
import ladysnake.pandemonium.common.entity.RunestoneGolemEntity;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
        @SuppressWarnings("unchecked") Class<? extends Task<?>> tc = (Class<? extends Task<?>>) task.getClass();
        return dissectTask(tc)
            .filter(taskTypeInfo -> taskTypeInfo.implClass() != null)
            .map(taskTypeInfo -> conceiveShadowClone(task, taskTypeInfo, currentHost, newHost));
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

            try {
                for (HexedField<?> field : info.fields()) {
                    field.copy(task, cursedChild, (value, f) -> {
                        if (value instanceof Task<?> t) return f.cast(attune(t, currentHost, newHost));
                        // Tasks delegate most entity-specific matters to lambda functions, which need to be converted
                        Object lambdaReplacement = FunctionalRecycler.tryRepurpose(value, f.field().getGenericType(), newHost);
                        if (lambdaReplacement != null) return f.cast(lambdaReplacement);
                        return value;
                    });
                }
            } catch (CloneFailedException e) {
                return null;    // yeet the child (leaving a half-initialized object to the garbage collector is fine right ?)
            }

            return cursedChild;
        } catch (InstantiationException e) {
            throw new UncheckedReflectionException("Failed to instantiate massively cursed goal", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean isInvalidSensor(Sensor<?> value, RunestoneGolemEntity runestoneGolemEntity) {
        TypeToken<?> parameterizedSensor = TypeToken.of(value.getClass()).getSupertype(Sensor.class);
        TypeToken<?> sensorArg = TypeToken.of(((ParameterizedType) parameterizedSensor.getType()).getActualTypeArguments()[0]);
        return !sensorArg.getRawType().isInstance(runestoneGolemEntity);
    }

    private record ClassInfo<T>(@Nullable Class<? extends T> implClass, Set<HexedField<?>> fields) {
    }

    private abstract static class ClassAnalyser<T> {
        private final Map<Class<? extends T>, Optional<ClassInfo<T>>> cache = new IdentityHashMap<>();
        private final Class<T> upperBound;

        ClassAnalyser(Class<T> upperBound) {
            this.upperBound = upperBound;
        }

        Optional<ClassInfo<T>> dissect(Class<? extends T> declaredType) {
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
                    if (Entity.class.isAssignableFrom(cntParam.getType())) {
                        if (!cntParam.getType().isAssignableFrom(RunestoneGolemEntity.class)) {
                            // This goal is expecting an owner incompatible with our golems,
                            // let's see if there is something usable higher up
                            return true;
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
            // Vindicators kinda just like unsafe casting
            return goalClass.getEnclosingClass() == VindicatorEntity.class;
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
            @SuppressWarnings("unchecked") TypeToken<? extends LivingEntity> ownerType = (TypeToken<? extends LivingEntity>) TypeToken.of(parameterizedTaskType.getActualTypeArguments()[0]);
            return !ownerType.isSupertypeOf(RunestoneGolemEntity.class);
        }
    }
}
