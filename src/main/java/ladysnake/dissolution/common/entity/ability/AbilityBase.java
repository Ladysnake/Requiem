package ladysnake.dissolution.common.entity.ability;

import ladysnake.dissolution.api.v1.entity.ability.MobAbility;
import net.minecraft.entity.mob.MobEntity;

public abstract class AbilityBase<E extends MobEntity> implements MobAbility<E> {
    protected final E owner;

    public AbilityBase(E owner) {
        this.owner = owner;
    }
}
