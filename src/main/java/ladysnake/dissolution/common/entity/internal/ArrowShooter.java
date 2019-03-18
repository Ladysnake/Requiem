package ladysnake.dissolution.common.entity.internal;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;

public interface ArrowShooter {
    ProjectileEntity invokeGetArrow(ItemStack arrowStack, float charge);
}
