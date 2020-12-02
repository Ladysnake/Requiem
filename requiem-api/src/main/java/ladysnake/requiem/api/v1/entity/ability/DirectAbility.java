package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface DirectAbility<E extends LivingEntity, T extends Entity> extends MobAbility<E> {
    /**
     * If the range is 0, the vanilla targeting system is used
     */
    double getRange();

    Class<T> getTargetType();

    boolean canTarget(T target);

    boolean trigger(T target);
}
