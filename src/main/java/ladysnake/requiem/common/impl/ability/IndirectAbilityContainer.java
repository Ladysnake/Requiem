package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import net.minecraft.entity.mob.MobEntity;

public class IndirectAbilityContainer<O extends MobEntity> extends AbilityContainer<IndirectAbility<O>> {
    protected IndirectAbilityContainer(IndirectAbility<O> ability) {
        super(ability);
    }

    public boolean trigger() {
        if (this.ability.trigger()) {
            this.setCooldown(this.ability.getCooldown());
            return true;
        }
        return false;
    }
}
