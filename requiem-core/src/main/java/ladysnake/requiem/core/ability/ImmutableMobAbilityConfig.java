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
package ladysnake.requiem.core.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.core.entity.ability.DirectAbilityBase;
import ladysnake.requiem.core.entity.ability.IndirectAbilityBase;
import ladysnake.requiem.core.entity.ability.MeleeAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.apiguardian.api.API;

import java.util.function.Function;

public class ImmutableMobAbilityConfig<E extends LivingEntity> implements MobAbilityConfig<E> {

    @API(status = API.Status.EXPERIMENTAL)
    public static <T extends LivingEntity> Function<T, DirectAbility<? super T, ?>> noneDirect(){
        return (mob) -> new DirectAbilityBase<T, Entity>(mob, 0, 0, Entity.class) {
            @Override
            public boolean canTarget(Entity target) {
                return false;
            }

            @Override
            public boolean run(Entity target) {
                return false;
            }
        };
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static <T extends LivingEntity> Function<T, IndirectAbility<? super T>> noneIndirect(){
        return (mob) -> new IndirectAbilityBase<T>(mob, 0) {
            @Override
            protected boolean run() {
                return false;
            }
        };
    }

    public static final MobAbilityConfig<MobEntity> DEFAULT = MobAbilityConfig.<MobEntity>builder().build();

    private final Function<E, DirectAbility<? super E, ?>> directAttackFactory;
    private final Function<E, IndirectAbility<? super E>> indirectAttackFactory;
    private final Function<E, DirectAbility<? super E, ?>> directInteractionFactory;
    private final Function<E, IndirectAbility<? super E>> indirectInteractionFactory;

    @API(status = API.Status.EXPERIMENTAL)
    public ImmutableMobAbilityConfig(Function<E, DirectAbility<? super E, ?>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory) {
        this(directAttackFactory, indirectAttackFactory, noneDirect(), noneIndirect());
    }

    @API(status = API.Status.EXPERIMENTAL)
    public ImmutableMobAbilityConfig(Function<E, DirectAbility<? super E, ?>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory, Function<E, DirectAbility<? super E, ?>> directInteractionFactory, Function<E, IndirectAbility<? super E>> indirectInteractionFactory) {
        this.directAttackFactory = directAttackFactory;
        this.indirectAttackFactory = indirectAttackFactory;
        this.directInteractionFactory = directInteractionFactory;
        this.indirectInteractionFactory = indirectInteractionFactory;
    }

    @Override
    public DirectAbility<? super E, ?> getDirectAbility(E mob, AbilityType type) {
        return (type == AbilityType.ATTACK ? directAttackFactory : directInteractionFactory).apply(mob);
    }

    @Override
    public IndirectAbility<? super E> getIndirectAbility(E mob, AbilityType type) {
        return (switch (type) {
            case ATTACK -> indirectAttackFactory;
            case INTERACT  -> indirectInteractionFactory;
        }).apply(mob);
    }

    public static class Builder<E extends MobEntity> implements MobAbilityConfig.Builder<E> {
        private Function<E, DirectAbility<? super E, ?>> directAttackFactory = MeleeAbility::new;
        private Function<E, IndirectAbility<? super E>> indirectAttackFactory = noneIndirect();
        private Function<E, DirectAbility<? super E, ?>> directInteractionFactory = noneDirect();
        private Function<E, IndirectAbility<? super E>> indirectInteractionFactory = noneIndirect();

        @Override
        public MobAbilityConfig.Builder<E> direct(AbilityType type, Function<E, DirectAbility<? super E, ?>> factory) {
            switch (type) {
                case ATTACK -> directAttackFactory = factory;
                case INTERACT -> directInteractionFactory = factory;
            }
            return this;
        }

        @Override
        public MobAbilityConfig.Builder<E> indirect(AbilityType type, Function<E, IndirectAbility<? super E>> factory) {
            switch (type) {
                case ATTACK -> indirectAttackFactory = factory;
                case INTERACT -> indirectInteractionFactory = factory;
            }
            return this;
        }

        @Override
        public MobAbilityConfig<E> build() {
            return new ImmutableMobAbilityConfig<>(directAttackFactory, indirectAttackFactory, directInteractionFactory, indirectInteractionFactory);
        }
    }
}
