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
 */
package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.common.entity.ability.MeleeAbility;
import net.minecraft.entity.mob.MobEntity;
import org.apiguardian.api.API;

import java.util.function.Function;

public class ImmutableMobAbilityConfig<E extends MobEntity> implements MobAbilityConfig<E> {

    @API(status = API.Status.EXPERIMENTAL)
    public static <T extends MobEntity> Function<T, DirectAbility<? super T>> noneDirect(){
        return (mob) -> (p, t) -> false;
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static <T extends MobEntity> Function<T, IndirectAbility<? super T>> noneIndirect(){
        return (mob) -> (p) -> false;
    }

    public static final MobAbilityConfig<MobEntity> DEFAULT = MobAbilityConfig.builder().build();

    private final Function<E, DirectAbility<? super E>> directAttackFactory;
    private final Function<E, IndirectAbility<? super E>> indirectAttackFactory;
    private final Function<E, DirectAbility<? super E>> directInteractionFactory;
    private final Function<E, IndirectAbility<? super E>> indirectInteractionFactory;

    @API(status = API.Status.EXPERIMENTAL)
    public ImmutableMobAbilityConfig(Function<E, DirectAbility<? super E>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory) {
        this(directAttackFactory, indirectAttackFactory, noneDirect(), noneIndirect());
    }

    @API(status = API.Status.EXPERIMENTAL)
    public ImmutableMobAbilityConfig(Function<E, DirectAbility<? super E>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory, Function<E, DirectAbility<? super E>> directInteractionFactory, Function<E, IndirectAbility<? super E>> indirectInteractionFactory) {
        this.directAttackFactory = directAttackFactory;
        this.indirectAttackFactory = indirectAttackFactory;
        this.directInteractionFactory = directInteractionFactory;
        this.indirectInteractionFactory = indirectInteractionFactory;
    }

    @Override
    public DirectAbility<? super E> getDirectAbility(E mob, AbilityType type) {
        return (type == AbilityType.ATTACK ? directAttackFactory : directInteractionFactory).apply(mob);
    }

    @Override
    public IndirectAbility<? super E> getIndirectAbility(E mob, AbilityType type) {
        return (type == AbilityType.ATTACK ? indirectAttackFactory : indirectInteractionFactory).apply(mob);
    }

    public static class Builder<E extends MobEntity> implements MobAbilityConfig.Builder<E> {
        private Function<E, DirectAbility<? super E>> directAttackFactory = MeleeAbility::new;
        private Function<E, IndirectAbility<? super E>> indirectAttackFactory = noneIndirect();
        private Function<E, DirectAbility<? super E>> directInteractionFactory = noneDirect();
        private Function<E, IndirectAbility<? super E>> indirectInteractionFactory = noneIndirect();

        @Override
        public MobAbilityConfig.Builder<E> direct(AbilityType type, Function<E, DirectAbility<? super E>> factory) {
            switch (type) {
                case ATTACK: directAttackFactory = factory; break;
                case INTERACT: directInteractionFactory = factory; break;
            }
            return this;
        }

        @Override
        public MobAbilityConfig.Builder<E> indirect(AbilityType type, Function<E, IndirectAbility<? super E>> factory) {
            switch (type) {
                case ATTACK: indirectAttackFactory = factory; break;
                case INTERACT: indirectInteractionFactory = factory; break;
            }
            return this;
        }

        @Override
        public MobAbilityConfig<E> build() {
            return new ImmutableMobAbilityConfig<>(directAttackFactory, indirectAttackFactory, directInteractionFactory, indirectInteractionFactory);
        }
    }
}
