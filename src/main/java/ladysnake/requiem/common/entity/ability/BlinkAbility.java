package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.common.util.RayHelper;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class BlinkAbility extends IndirectAbilityBase<MobEntity> {
    private int cooldown;

    public BlinkAbility(MobEntity owner) {
        super(owner);
    }

    @Override
    public void update() {
        cooldown--;
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (cooldown <= 0) {
            Vec3d blinkPos = RayHelper.findBlinkPos(this.owner, 1F, 32D);
            if (this.owner.teleport(blinkPos.x, blinkPos.y, blinkPos.z, true)) {
                this.owner.world.playSound(null, this.owner.prevX, this.owner.prevY, this.owner.prevZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.owner.getSoundCategory(), 1.0F, 1.0F);
                this.owner.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
        return false;
    }
}
