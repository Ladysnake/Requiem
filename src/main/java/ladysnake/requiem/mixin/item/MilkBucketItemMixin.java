package ladysnake.requiem.mixin.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.tag.EntityTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
    @Inject(method = "onItemFinishedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearPotionEffects()Z"))
    private void regenSkeletons(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (EntityTags.SKELETONS.contains(user.getType())) {
            user.heal(2f);
        }
    }
}
