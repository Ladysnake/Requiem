package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import net.minecraft.entity.mob.MobEntity;

public abstract class IndirectAbilityBase<E extends MobEntity> extends AbilityBase<E> implements IndirectAbility<E> {
    public IndirectAbilityBase(E owner) {
        super(owner);
    }
}
