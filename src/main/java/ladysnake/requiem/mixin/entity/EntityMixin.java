package ladysnake.requiem.mixin.entity;

import ladysnake.requiem.common.util.RequiemEntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements RequiemEntityExtension {

    @Shadow
    protected abstract float getEyeHeight(EntityPose pose, EntityDimensions dimensions);

    @Shadow
    public abstract EntityDimensions getDimensions(EntityPose pose);

    @Shadow
    public abstract EntityPose getPose();

    @Override
    public float getEyeHeight() {
        EntityPose pose = this.getPose();
        return this.getEyeHeight(pose, this.getDimensions(pose));
    }
}
