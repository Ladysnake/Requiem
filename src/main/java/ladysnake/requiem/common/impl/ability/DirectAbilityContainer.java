package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class DirectAbilityContainer<O extends LivingEntity, T extends Entity> extends AbilityContainer<DirectAbility<O, T>> {
    public DirectAbilityContainer(DirectAbility<O, T> ability) {
        super(ability);
    }

    public double getRange() {
        return this.ability.getRange();
    }

    public boolean canTrigger(Entity target) {
        if (this.getCooldown() > 0) return false;

        Class<T> targetType = ability.getTargetType();

        if (targetType.isInstance(target)) {
            return ability.canTarget(targetType.cast(target));
        }

        return false;
    }

    public boolean trigger(Entity target) {
        boolean success;
        if (ability.getTargetType().isInstance(target)) {
            success = ability.trigger(ability.getTargetType().cast(target));

            if (success) {
                this.setCooldown(ability.getCooldown());
            }
        } else {
            success = false;
        }

        return success;
    }
}
