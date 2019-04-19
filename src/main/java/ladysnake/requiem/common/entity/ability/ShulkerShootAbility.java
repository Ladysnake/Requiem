package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BoundingBox;

import javax.annotation.Nullable;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class ShulkerShootAbility extends IndirectAbilityBase<ShulkerEntity> implements DirectAbility<ShulkerEntity> {
    private static final Function<ShulkerEntity, ? extends Goal> SHOOT_GOAL_FACTORY;

    private Goal shootBulletGoal;
    private boolean started;

    public ShulkerShootAbility(ShulkerEntity owner) {
        super(owner);
        this.shootBulletGoal = SHOOT_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (!this.started) {
            return this.trigger(player, this.owner.world.getClosestEntity(
                    LivingEntity.class,
                    new TargetPredicate(),
                    this.owner,
                    this.owner.x,
                    this.owner.y + (double)this.owner.getStandingEyeHeight(),
                    this.owner.z,
                    this.getSearchBox(16.0)));
        }
        return false;
    }

    @Override
    public boolean trigger(PlayerEntity player, @Nullable Entity target) {
        if (!this.started && target instanceof LivingEntity) {
            this.owner.setTarget((LivingEntity) target);
            if (this.shootBulletGoal.canStart()) {
                this.shootBulletGoal.start();
                this.started = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public void update() {
        if (started) {
            if (this.shootBulletGoal.shouldContinue()) {
                this.shootBulletGoal.tick();
            } else {
                started = false;
                this.shootBulletGoal.stop();
            }
        }
    }

    private BoundingBox getSearchBox(double range) {
        return this.owner.getBoundingBox().expand(range, 4.0D, range);
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1606$class_1607", "net.minecraft.entity.mob.ShulkerEntity$ShootBulletGoal"));
            SHOOT_GOAL_FACTORY = ReflectionHelper.createFactory(
                    clazz,
                    "apply",
                    Function.class,
                    ReflectionHelper.getTrustedLookup(clazz),
                    MethodType.methodType(Object.class, Object.class),
                    ShulkerEntity.class
            );
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ShootBulletGoal class", e);
        }
    }
}
