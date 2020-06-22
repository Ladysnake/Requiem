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
package ladysnake.requiem.common.gamerule;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import ladysnake.requiem.mixin.world.GameRulesAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameruleHelper {
    @SuppressWarnings("rawtypes")
    private static final Constructor<GameRules.RuleType> RULE_TYPE_CNTR;

    public static <T extends GameRules.Rule<T>> GameRules.RuleKey<T> register(String name, GameRules.RuleType<T> type) {
        return GameRulesAccessor.invokeRegister(name, type);
    }

    public static <E extends Enum<E>> GameRules.RuleType<EnumRule<E>> createEnumRule(Class<E> enumType, E value, BiConsumer<MinecraftServer, EnumRule<E>> notifier) {
        Preconditions.checkArgument(enumType.isInstance(value));
        Preconditions.checkArgument(enumType.isEnum());
        Preconditions.checkArgument(enumType.getEnumConstants().length > 0);
        return createRuleType(() -> new EnumArgumentType<>(enumType), (type) -> new EnumRule<>(type, enumType, value), notifier);
    }

    public static <E extends Enum<E>> GameRules.RuleType<EnumRule<E>> createEnumRule(E initialValue) {
        return createEnumRule(initialValue.getDeclaringClass(), initialValue, (server, rule) -> {});
    }

    public static GameRules.RuleType<GameRules.BooleanRule> createBooleanRule(boolean initialValue) {
        return createBooleanRule(initialValue, (server, rule) -> {});
    }

    public static GameRules.RuleType<GameRules.BooleanRule> createBooleanRule(boolean initialValue, BiConsumer<MinecraftServer, GameRules.BooleanRule> changeCallback) {
        return createRuleType(BoolArgumentType::bool, type -> new GameRules.BooleanRule(type, initialValue), changeCallback);
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameRules.Rule<T>> GameRules.RuleType<T> createRuleType(Supplier<ArgumentType<?>> argumentType, Function<GameRules.RuleType<T>, T> factory, BiConsumer<MinecraftServer, T> notifier) {
        try {
            return RULE_TYPE_CNTR.newInstance(argumentType, factory, notifier);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UncheckedReflectionException("Failed to instantiate RuleType", e);
        }
    }

    static {
        try {
            RULE_TYPE_CNTR = GameRules.RuleType.class.getDeclaredConstructor(Supplier.class, Function.class, BiConsumer.class);
            RULE_TYPE_CNTR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new UncheckedReflectionException("Failed to reflect the constructor of GameRules.RuleType", e);
        }
    }
}
