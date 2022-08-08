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

import ladysnake.pandemonium.common.entity.RunestoneGolemEntity;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public final class FunctionalRecycler {
    @Nullable
    static Object tryRepurpose(Object value, Type type, RunestoneGolemEntity newHost) {
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == Predicate.class) {
                return repurpose((Predicate<?>) value, parameterizedType.getActualTypeArguments()[0], newHost);
            } else if (parameterizedType.getRawType() == Consumer.class) {
                return repurpose((Consumer<?>) value, parameterizedType.getActualTypeArguments()[0], newHost);
            } else if (parameterizedType.getRawType() == Function.class) {
                return repurpose((Function<?, ?>) value, parameterizedType.getActualTypeArguments()[0], parameterizedType.getActualTypeArguments()[1], newHost);
            } else if (parameterizedType.getRawType() == ToIntFunction.class) {
                return repurpose((ToIntFunction<?>) value, parameterizedType.getActualTypeArguments()[0], newHost);
            } else if (parameterizedType.getRawType() == ToDoubleFunction.class) {
                return repurpose((ToDoubleFunction<?>) value, parameterizedType.getActualTypeArguments()[0], newHost);
            } else if (parameterizedType.getRawType() == BiConsumer.class) {
                return repurpose((BiConsumer<?, ?>) value, parameterizedType.getActualTypeArguments()[0], parameterizedType.getActualTypeArguments()[1], newHost);
            } else if (((Class<?>) parameterizedType.getRawType()).getPackageName().equals("java.util.function")) {
                throw new CloneFailedException("Unsupported standard functional interface " + type);
            }
        }
        return null;
    }

    private static Predicate<?> repurpose(Predicate<?> predicate, Type predicateArgument, MobEntity newHost) {
        if (isNotEntityType(predicateArgument)) {
            // Oh, it does not care about entities at all
            return predicate;
        }

        @SuppressWarnings("unchecked") Predicate<Object> p = (Predicate<Object>) predicate;
        try {
            p.test(newHost);    // bingo, it's a generic predicate
            return predicate;
        } catch (ClassCastException e) {
            try {
                boolean constantResult = p.test(null);   // ohkay, it's not generic but it doesn't use its argument
                return o -> constantResult;
            } catch (NullPointerException npe) {
                throw new CloneFailedException("Un-reshapable predicate");
            }
        }
    }

    private static Consumer<?> repurpose(Consumer<?> consumer, Type consumerArgument, MobEntity newHost) {
        if (isNotEntityType(consumerArgument)) {
            return consumer;
        }

        @SuppressWarnings("unchecked") Consumer<Object> c = (Consumer<Object>) consumer;
        // consumers have side effects, so we cannot just test them randomly
        return o -> {
            try {
                c.accept(o);
            } catch (ClassCastException e) {
                Requiem.LOGGER.error("Failed to do a thing", e);
            }
        };
    }

    private static BiConsumer<?, ?> repurpose(BiConsumer<?, ?> consumer, Type consumerArgument1, Type consumerArgument2, MobEntity newHost) {
        if (isNotEntityType(consumerArgument1) && isNotEntityType(consumerArgument2)) {
            return consumer;
        }

        @SuppressWarnings("unchecked") BiConsumer<Object, Object> c = (BiConsumer<Object, Object>) consumer;
        // consumers have side effects, so we cannot just test them randomly
        return (o1, o2) -> {
            try {
                c.accept(o1, o2);
            } catch (ClassCastException e) {
                Requiem.LOGGER.error("Failed to do a thing", e);
            }
        };
    }

    private static Function<?, ?> repurpose(Function<?, ?> function, Type functionArgument, Type functionResult, MobEntity newHost) {
        if (isNotEntityType(functionArgument)) {
            return function;
        }

        if (functionResult == SoundEvent.class) {
            return o -> SoundEvents.ENTITY_IRON_GOLEM_STEP;
        }

        @SuppressWarnings("unchecked") Function<Object, ?> f = (Function<Object, ?>) function;
        try {
            f.apply(newHost); // bingo, it's a generic function
            return function;
        } catch (ClassCastException e) {
            try {
                Object constantResult = f.apply(null);
                return o -> constantResult;
            } catch (NullPointerException npe) {
                throw new CloneFailedException("Un-reshapable function");
            }
        }
    }

    private static ToDoubleFunction<?> repurpose(ToDoubleFunction<?> function, Type functionArgument, MobEntity newHost) {
        if (isNotEntityType(functionArgument)) {
            return function;
        }

        @SuppressWarnings("unchecked") ToDoubleFunction<Object> f = (ToDoubleFunction<Object>) function;
        try {
            f.applyAsDouble(newHost); // bingo, it's a generic function
            return function;
        } catch (ClassCastException e) {
            try {
                double constantResult = f.applyAsDouble(null);
                return o -> constantResult;
            } catch (NullPointerException npe) {
                throw new CloneFailedException("Un-reshapable function");
            }
        }
    }

    private static ToIntFunction<?> repurpose(ToIntFunction<?> function, Type functionArgument, MobEntity newHost) {
        if (isNotEntityType(functionArgument)) {
            return function;
        }

        @SuppressWarnings("unchecked") ToIntFunction<Object> f = (ToIntFunction<Object>) function;
        try {
            f.applyAsInt(newHost); // bingo, it's a generic function
            return function;
        } catch (ClassCastException e) {
            try {
                int constantResult = f.applyAsInt(null);
                return o -> constantResult;
            } catch (NullPointerException npe) {
                throw new CloneFailedException("Un-reshapable function");
            }
        }
    }

    private static boolean isNotEntityType(Type consumerArgument1) {
        return !(consumerArgument1 instanceof Class<?> klass && Entity.class.isAssignableFrom(klass));
    }
}
