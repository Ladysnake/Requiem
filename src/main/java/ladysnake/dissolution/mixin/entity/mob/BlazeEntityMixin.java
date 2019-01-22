package ladysnake.dissolution.mixin.entity.mob;

import ladysnake.dissolution.api.v1.entity.TriggerableAttacker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlazeEntity.class)
public abstract class BlazeEntityMixin extends MobEntity implements TriggerableAttacker {
    @Shadow public abstract void setFireActive(boolean boolean_1);

    @Shadow public abstract boolean isFireActive();

    private int dissolution_fireTicks;

    protected BlazeEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    public void updateFire(CallbackInfo info) {
        if (this.isFireActive() && --dissolution_fireTicks < 0) {
            this.setFireActive(false);
        }
    }

    @Override
    public boolean triggerIndirectAttack(PlayerEntity player) {
        double double_1 = 25.0;
        float float_1 = MathHelper.sqrt(MathHelper.sqrt(double_1)) * 0.5F;
        Vec3d rot = this.getRotationVec(1.0f).multiply(10);

        this.world.fireWorldEvent(null, 1018, new BlockPos((int)this.x, (int)this.y, (int)this.z), 0);
        this.dissolution_fireTicks = 200;
        this.setFireActive(true);
        SmallFireballEntity smallFireballEntity_1 = new SmallFireballEntity(
                this.world,
                this,
                rot.x + this.getRand().nextGaussian() * (double)float_1,
                rot.y,
                rot.z + this.getRand().nextGaussian() * (double)float_1
        );
        smallFireballEntity_1.y = this.y + (double)(this.height / 2.0F) + 0.5D;
        this.world.spawnEntity(smallFireballEntity_1);
        return true;
    }
}
