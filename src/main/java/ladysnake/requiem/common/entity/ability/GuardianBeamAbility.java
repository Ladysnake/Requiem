package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class GuardianBeamAbility extends DirectAbilityBase<GuardianEntity> {
    private static final Function<GuardianEntity, ? extends Goal> BEAM_GOAL_FACTORY;

    private final Goal fireBeamGoal;
    private boolean started;

    public GuardianBeamAbility(GuardianEntity owner) {
        super(owner);
        this.fireBeamGoal = BEAM_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            owner.setTarget(target);
            if (fireBeamGoal.canStart()) {
                fireBeamGoal.start();
                success = true;
            }
        }
        return success;
    }

    @Override
    public void update() {
        if (started && fireBeamGoal.shouldContinue()) {
            fireBeamGoal.tick();
        } else {
            started = false;
        }
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1577$class_1578", "net.minecraft.entity.mob.GuardianEntity$FireBeamGoal"));
            BEAM_GOAL_FACTORY = ReflectionHelper.createFactory(
                    clazz,
                    "apply",
                    Function.class,
                    ReflectionHelper.getTrustedLookup(clazz),
                    MethodType.methodType(Object.class, Object.class),
                    GuardianEntity.class
            );
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal class", e);
        }
    }
}
