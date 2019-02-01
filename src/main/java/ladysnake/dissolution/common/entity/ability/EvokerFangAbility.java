package ladysnake.dissolution.common.entity.ability;

import ladysnake.reflectivefabric.reflection.ReflectionHelper;
import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import ladysnake.reflectivefabric.reflection.typed.TypedMethod0;
import ladysnake.reflectivefabric.reflection.typed.TypedMethodHandles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.reflectivefabric.reflection.ReflectionHelper.pick;

public class EvokerFangAbility extends DirectAbilityBase<EvokerEntity> {
    public static final Function<EvokerEntity, ? extends SpellcastingIllagerEntity.CastSpellGoal> FANGS_GOAL_FACTORY;
    public static final TypedMethod0<SpellcastingIllagerEntity.CastSpellGoal, Void> CAST_SPELL_GOAL$CAST_SPELL = TypedMethodHandles.findVirtual(SpellcastingIllagerEntity.CastSpellGoal.class, pick("method_7148", "castSpell"), void.class);

    private final SpellcastingIllagerEntity.CastSpellGoal conjureFangsGoal;

    public EvokerFangAbility(EvokerEntity owner) {
        super(owner);
        this.conjureFangsGoal = FANGS_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            owner.setTarget(target);
            if (conjureFangsGoal.canStart()) {
                CAST_SPELL_GOAL$CAST_SPELL.invoke(conjureFangsGoal);
                success = true;
            }
            owner.setTarget(null);
        }
        return success;
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1564$class_1565", "net.minecraft.entity.mob.EvokerEntity$ConjureFangsGoal"));
            FANGS_GOAL_FACTORY = ReflectionHelper.createFactory(
                    clazz,
                    "apply",
                    Function.class,
                    ReflectionHelper.getTrustedLookup(clazz),
                    MethodType.methodType(Object.class, Object.class),
                    EvokerEntity.class
            );
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal class", e);
        }
    }
}
