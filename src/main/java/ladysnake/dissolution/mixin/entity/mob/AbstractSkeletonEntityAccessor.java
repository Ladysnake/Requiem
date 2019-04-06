package ladysnake.dissolution.mixin.entity.mob;

import ladysnake.dissolution.common.entity.internal.ArrowShooter;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityAccessor implements ArrowShooter {
    @Invoker("createArrowProjectile")
    public abstract ProjectileEntity invokeGetArrow(ItemStack arrowStack, float charge);
}
