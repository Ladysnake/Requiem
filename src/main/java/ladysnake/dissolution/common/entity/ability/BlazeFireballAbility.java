package ladysnake.dissolution.common.entity.ability;

import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlazeFireballAbility extends IndirectAbilityBase<MobEntity> {
    private int fireTicks;

    public BlazeFireballAbility(MobEntity owner) {
        super(owner);
    }

    @Override
    public void update() {
        if (this.owner instanceof BlazeEntity && ((BlazeEntity) this.owner).isFireActive() && --fireTicks < 0) {
            ((BlazeEntity) this.owner).setFireActive(false);
        }
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        double double_1 = 25.0;
        float float_1 = MathHelper.sqrt(MathHelper.sqrt(double_1)) * 0.5F;
        Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);

        this.owner.world.playEvent(null, 1018, new BlockPos((int)this.owner.x, (int)this.owner.y, (int)this.owner.z), 0);
        if (this.owner instanceof BlazeEntity) {
            this.fireTicks = 200;
            ((BlazeEntity) this.owner).setFireActive(true);
        }
        SmallFireballEntity smallFireballEntity_1 = new SmallFireballEntity(
                this.owner.world,
                this.owner,
                rot.x + this.owner.getRand().nextGaussian() * (double)float_1,
                rot.y,
                rot.z + this.owner.getRand().nextGaussian() * (double)float_1
        );
        smallFireballEntity_1.y = this.owner.y + (double)(this.owner.getHeight() / 2.0F) + 0.5D;
        this.owner.world.spawnEntity(smallFireballEntity_1);
        return true;
    }
}
