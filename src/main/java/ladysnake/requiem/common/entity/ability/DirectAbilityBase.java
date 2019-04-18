package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import net.minecraft.entity.mob.MobEntity;

public abstract class DirectAbilityBase<E extends MobEntity> extends AbilityBase<E> implements DirectAbility<E> {
    public DirectAbilityBase(E owner) {
        super(owner);
    }
}
