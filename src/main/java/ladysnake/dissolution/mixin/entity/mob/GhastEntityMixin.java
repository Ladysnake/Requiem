package ladysnake.dissolution.mixin.entity.mob;

import ladysnake.dissolution.api.v1.entity.TriggerableAttacker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GhastEntity.class)
public abstract class GhastEntityMixin extends FlyingEntity implements TriggerableAttacker {
    @Shadow public abstract int getFireballStrength();

    private int dissolution_fireballCooldown = -40;
    protected GhastEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "update", at = @At("HEAD"))
    public void updateFire(CallbackInfo info) {
        this.dissolution_fireballCooldown++;
    }

    @Override
    public boolean triggerIndirectAttack(PlayerEntity player) {
        if (this.dissolution_fireballCooldown >= 20) {
            Vec3d vec3d_1 = this.getRotationVec(1.0F);
            Vec3d rot = this.getRotationVec(1.0f).multiply(10);
            world.fireWorldEvent(null, 1016, new BlockPos(this), 0);
            FireballEntity fireballEntity_1 = new FireballEntity(world, this, rot.x, rot.y, rot.z);
            fireballEntity_1.explosionPower = this.getFireballStrength();
            fireballEntity_1.x = this.x + vec3d_1.x * 4.0D;
            fireballEntity_1.y = this.y + (double)(this.height / 2.0F) + 0.5D;
            fireballEntity_1.z = this.z + vec3d_1.z * 4.0D;
            world.spawnEntity(fireballEntity_1);
            this.dissolution_fireballCooldown = -40;
            return true;
        }
        return false;
    }
}
