package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class EvokerFangAbility extends DirectAbilityBase<EvokerEntity> {
    private static final Function<EvokerEntity, ? extends SpellcastingIllagerEntity.CastSpellGoal> FANGS_GOAL_FACTORY;
    private static final MethodHandle CAST_SPELL_GOAL$CAST_SPELL;

    static {
        try {
            CAST_SPELL_GOAL$CAST_SPELL = MethodHandles.lookup().findVirtual(SpellcastingIllagerEntity.CastSpellGoal.class, pick("method_7148", "castSpell"), MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }
    }

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
                try {
                    CAST_SPELL_GOAL$CAST_SPELL.invokeExact(conjureFangsGoal);
                } catch (Throwable throwable) {
                    throw new UncheckedReflectionException("Failed to trigger evoker fang ability", throwable);
                }
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
