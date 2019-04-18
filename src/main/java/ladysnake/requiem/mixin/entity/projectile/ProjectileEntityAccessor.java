package ladysnake.requiem.mixin.entity.projectile;

import ladysnake.requiem.common.entity.internal.ItemStackConvertible;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArrowEntity.class)
public abstract class ProjectileEntityAccessor implements ItemStackConvertible {
    @Invoker("asItemStack")
    public abstract ItemStack invokeAsItemStack();
}
