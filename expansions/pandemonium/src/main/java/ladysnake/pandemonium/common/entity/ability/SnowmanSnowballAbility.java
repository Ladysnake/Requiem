package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.thrown.SnowballEntity;
import net.minecraft.sound.SoundEvents;

public class SnowmanSnowballAbility<T extends MobEntity> extends IndirectAbilityBase<T> {
    public SnowmanSnowballAbility(T owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        SnowballEntity snowball = new SnowballEntity(this.owner.world, this.owner);
        snowball.method_19207(player, player.pitch, player.yaw, 0.0F, 1.5F, 1.0F);
        this.owner.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0F, 1.0F / (this.owner.getRand().nextFloat() * 0.4F + 0.8F));
        this.owner.world.spawnEntity(snowball);
        return true;
    }
}
