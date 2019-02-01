package ladysnake.dissolution.common.entity.ability;

import ladysnake.reflectivefabric.reflection.ReflectionHelper;
import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.dissolution.common.entity.ability.EvokerFangAbility.CAST_SPELL_GOAL$CAST_SPELL;
import static ladysnake.reflectivefabric.reflection.ReflectionHelper.pick;

public class EvokerVexAbility extends IndirectAbilityBase<EvokerEntity> {
    private static final Function<EvokerEntity, ? extends SpellcastingIllagerEntity.CastSpellGoal> VEX_GOAL_FACTORY;

    private final SpellcastingIllagerEntity.CastSpellGoal summonVexGoal;

    public EvokerVexAbility(EvokerEntity owner) {
        super(owner);
        summonVexGoal = VEX_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        boolean success = false;
        owner.setTarget(owner); // The target needs to be non null to let the goal run
        if (summonVexGoal.canStart()) {
            CAST_SPELL_GOAL$CAST_SPELL.invoke(summonVexGoal);
            owner.setTarget(null);
            success = true;
        }
        owner.setTarget(null);
        return success;
    }

    @Override
    public void update() {
        owner.setTarget(owner);
        if (summonVexGoal.shouldContinue()) {
            summonVexGoal.tick();
        }
        owner.setTarget(null);
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1564$class_1567", "net.minecraft.entity.mob.EvokerEntity$SummonVexGoal"));
            VEX_GOAL_FACTORY = ReflectionHelper.createFactory(
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
