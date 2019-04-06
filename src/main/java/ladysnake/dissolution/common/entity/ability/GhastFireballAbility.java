package ladysnake.dissolution.common.entity.ability;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GhastFireballAbility extends IndirectAbilityBase<MobEntity> {
    private int fireballCooldown = -40;

    public GhastFireballAbility(MobEntity owner) {
        super(owner);
    }

    @Override
    public void update() {
        this.fireballCooldown++;
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        if (this.fireballCooldown >= 20) {
            Vec3d vec3d_1 = this.owner.getRotationVec(1.0F);
            Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
            // method_8444 == playEvent
            this.owner.world.method_8444(null, 1016, new BlockPos(this.owner), 0);
            FireballEntity fireballEntity_1 = new FireballEntity(this.owner.world, this.owner, rot.x, rot.y, rot.z);
            fireballEntity_1.explosionPower = this.owner instanceof GhastEntity ? ((GhastEntity) this.owner).getFireballStrength() : 1;
            fireballEntity_1.x = this.owner.x + vec3d_1.x * 4.0D;
            fireballEntity_1.y = this.owner.y + (double)(this.owner.getHeight() / 2.0F) + 0.5D;
            fireballEntity_1.z = this.owner.z + vec3d_1.z * 4.0D;
            this.owner.world.spawnEntity(fireballEntity_1);
            this.fireballCooldown = -40;
            return true;
        }
        return false;
    }
}
