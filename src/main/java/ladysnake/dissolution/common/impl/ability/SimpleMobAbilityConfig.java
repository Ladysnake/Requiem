package ladysnake.dissolution.common.impl.ability;

import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.DirectAbility;
import ladysnake.dissolution.api.v1.entity.ability.IndirectAbility;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;

import java.util.function.Function;

public class SimpleMobAbilityConfig<E extends MobEntity> implements MobAbilityConfig<E> {
    public static <T extends MobEntity> Function<T, DirectAbility<? super T>> defaultMelee() {
        return (mob) -> (player, target) -> {
            // We actually need to check if the entity has an attack damage attribute, because mojang doesn't.
            boolean success = mob.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null && mob.method_6121(target);
            if (success && target instanceof LivingEntity) {
                player.getMainHandStack().onEntityDamaged((LivingEntity) target, player);
            }
            return success;
        };
    }

    public static <T extends MobEntity> Function<T, DirectAbility<? super T>> noneDirect(){
        return (mob) -> (p, t) -> false;
    }

    public static <T extends MobEntity> Function<T, IndirectAbility<? super T>> noneIndirect(){
        return (mob) -> (p) -> false;
    }

    public static final MobAbilityConfig<MobEntity> DEFAULT = new SimpleMobAbilityConfig<>(defaultMelee(), noneIndirect(), noneDirect(), noneIndirect());

    private final Function<E, DirectAbility<? super E>> directAttackFactory;
    private final Function<E, IndirectAbility<? super E>> indirectAttackFactory;
    private final Function<E, DirectAbility<? super E>> directInteractionFactory;
    private final Function<E, IndirectAbility<? super E>> indirectInteractionFactory;

    public SimpleMobAbilityConfig(Function<E, DirectAbility<? super E>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory) {
        this(directAttackFactory, indirectAttackFactory, noneDirect(), noneIndirect());
    }

    public SimpleMobAbilityConfig(Function<E, DirectAbility<? super E>> directAttackFactory, Function<E, IndirectAbility<? super E>> indirectAttackFactory, Function<E, DirectAbility<? super E>> directInteractionFactory, Function<E, IndirectAbility<? super E>> indirectInteractionFactory) {
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
}
