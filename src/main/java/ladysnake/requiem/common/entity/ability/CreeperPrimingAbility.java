package ladysnake.requiem.common.entity.ability;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CreeperPrimingAbility extends IndirectAbilityBase<CreeperEntity> {
    public CreeperPrimingAbility(CreeperEntity owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        this.owner.setFuseSpeed(this.owner.getFuseSpeed() > 0 ? -1 : 1);
        return true;
    }
}
