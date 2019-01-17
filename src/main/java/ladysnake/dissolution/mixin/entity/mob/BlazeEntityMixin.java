package ladysnake.dissolution.mixin.entity.mob;

import ladysnake.dissolution.api.entity.TriggerableAttacker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlazeEntity.class)
public abstract class BlazeEntityMixin extends MobEntity implements TriggerableAttacker {

    protected BlazeEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    public boolean triggerIndirectAttack(PlayerEntity player) {
        double double_1 = 25.0;
        float float_1 = MathHelper.sqrt(MathHelper.sqrt(double_1)) * 0.5F;
        Vec3d rot = this.getRotationVec(1.0f).multiply(10);

        for(int i = 0; i < 1; ++i) {
            SmallFireballEntity smallFireballEntity_1 = new SmallFireballEntity(
                    this.world,
                    this,
                    rot.x + this.getRand().nextGaussian() * (double)float_1,
                    rot.y,
                    rot.z + this.getRand().nextGaussian() * (double)float_1
            );
            smallFireballEntity_1.y = this.y + (double)(this.height / 2.0F) + 0.5D;
            this.world.spawnEntity(smallFireballEntity_1);
        }
        return true;
    }
}
