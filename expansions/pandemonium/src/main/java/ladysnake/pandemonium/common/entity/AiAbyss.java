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

import com.google.common.base.Preconditions;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class AiAbyss {
    private static final Unsafe abholos;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            abholos = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Failed to summon The Unsafe One", e);
        }
    }

    private static final Map<Class<? extends Goal>, Optional<GoalTypeInfo>> goalInfos = new IdentityHashMap<>();

    public static Optional<Goal> attune(Goal goal, MobEntity currentHost, RunestoneGolemEntity newHost) {
        return dissect(goal.getClass())
            .filter(goalTypeInfo -> goalTypeInfo.implClass() != null)
            .map(goalTypeInfo -> conceiveShadowClone(goal, goalTypeInfo, currentHost, newHost));
    }

    private static Optional<GoalTypeInfo> dissect(Class<? extends Goal> declaredType) {
        return goalInfos.computeIfAbsent(declaredType, gc -> {
            @Nullable @SuppressWarnings("unchecked") Class<? extends Goal> superclass = gc == Goal.class
                ? null
                : (Class<? extends Goal>) gc.getSuperclass();

            Optional<GoalTypeInfo> parentInfo = superclass == null ? Optional.empty() : dissect(superclass);

            if (shouldBeSubstitutedWithAncestor(gc, parentInfo.orElse(null))) {
                return parentInfo;
            }

            Set<HexedField> fields = new HashSet<>(parentInfo.map(GoalTypeInfo::fields).orElse(Set.of()));

            for (Field declaredField : gc.getDeclaredFields()) {
                if (!Modifier.isStatic(declaredField.getModifiers())) {
                    declaredField.setAccessible(true);
                    fields.add(hex(declaredField));
                }
            }

            // Can't instantiate an abstract class, so we just keep field info for subtypes
            Class<? extends Goal> implClass = Modifier.isAbstract(gc.getModifiers()) ? null : gc;

            return Optional.of(new GoalTypeInfo(implClass, fields));
        });
    }

    private static boolean shouldBeSubstitutedWithAncestor(Class<? extends Goal> gc, @Nullable GoalTypeInfo parentInfo) {
        // If the parent is supposed to be substituted, obviously this too
        if (parentInfo != null && parentInfo.implClass() != null && parentInfo.implClass() != gc.getSuperclass()) {
            return true;
        }

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
        return goalClass.getEnclosingClass() == VindicatorEntity.class; // hello yes unsafe cast go brr
    }

    // Hex kind of like byte manipulation, but mostly as in curse
    private static HexedField hex(Field target) {
        long offset = abholos.objectFieldOffset(target);
        Class<?> targetType = target.getType();
        if (targetType == boolean.class) {
            return (from, to, objectMapper) -> abholos.putBoolean(to, offset, abholos.getBoolean(from, offset));
        } else if (targetType == byte.class) {
            return (from, to, objectMapper) -> abholos.putByte(to, offset, abholos.getByte(from, offset));
        } else if (targetType == short.class) {
            return (from, to, objectMapper) -> abholos.putShort(to, offset, abholos.getShort(from, offset));
        } else if (targetType == char.class) {
            return (from, to, objectMapper) -> abholos.putChar(to, offset, abholos.getChar(from, offset));
        } else if (targetType == int.class) {
            return (from, to, objectMapper) -> abholos.putInt(to, offset, abholos.getInt(from, offset));
        } else if (targetType == float.class) {
            return (from, to, objectMapper) -> abholos.putFloat(to, offset, abholos.getFloat(from, offset));
        } else if (targetType == long.class) {
            return (from, to, objectMapper) -> abholos.putLong(to, offset, abholos.getLong(from, offset));
        } else if (targetType == double.class) {
            return (from, to, objectMapper) -> abholos.putDouble(to, offset, abholos.getDouble(from, offset));
        } else if (targetType.isPrimitive()) {
            throw new IllegalStateException("Unknown primitive type " + targetType);
        }
        return (from, to, objectMapper) -> abholos.putObject(to, offset, objectMapper.apply(abholos.getObject(from, offset)));
    }

    private static Goal conceiveShadowClone(Goal goal, GoalTypeInfo info, MobEntity currentHost, RunestoneGolemEntity newHost) {
        Preconditions.checkNotNull(info.implClass());
        Preconditions.checkState(Goal.class.isAssignableFrom(info.implClass()) && !Modifier.isAbstract(info.implClass().getModifiers()));

        try {
            Goal cursedChild = (Goal) abholos.allocateInstance(info.implClass());

            for (HexedField field : info.fields()) {
                field.copy(goal, cursedChild, value -> value == currentHost ? newHost : value);
            }

            return cursedChild;
        } catch (InstantiationException e) {
            throw new UncheckedReflectionException("Failed to instantiate massively cursed goal", e);
        }
    }

    private record GoalTypeInfo(@Nullable Class<? extends Goal> implClass, Set<HexedField> fields) {
    }

    private interface HexedField {
        void copy(Object from, Object to, UnaryOperator<Object> objectMapper);
    }
}
