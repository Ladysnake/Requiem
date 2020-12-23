package ladysnake.requiem.mixin.common.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ArmorItem.class, ElytraItem.class})
public abstract class ArmorItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void cancelUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (InventoryLimiter.KEY.get(user).isLocked(InventoryPart.ARMOR)) {
            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }
}
