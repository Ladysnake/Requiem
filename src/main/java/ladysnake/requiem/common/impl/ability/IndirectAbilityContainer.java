package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbility;
import net.minecraft.entity.LivingEntity;

public class IndirectAbilityContainer<O extends LivingEntity> extends AbilityContainer<IndirectAbility<O>> {
    protected IndirectAbilityContainer(IndirectAbility<O> ability) {
        super(ability);
    }

    public boolean trigger() {
        MobAbility.Result result = this.ability.trigger();
        if (result.resetsCooldown()) {
            this.setCooldown(this.ability.getCooldown());
        }
        return result.isSuccess();
    }
}
