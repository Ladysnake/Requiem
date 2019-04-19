package ladysnake.requiem.common.entity.ability;

import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ShulkerPeekAbility extends IndirectAbilityBase<ShulkerEntity> {
    public ShulkerPeekAbility(ShulkerEntity owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (this.owner.getPeekAmount() > 0) {
            this.owner.setPeekAmount(0);
        } else {
            this.owner.setPeekAmount(100);
        }
        return true;
    }
}
