package ladysnake.requiem.mixin.entity;

import ladysnake.requiem.api.v1.event.ItemPickupCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(
            method = "onPlayerCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void fireItemPickupEvent(PlayerEntity playerEntity_1, CallbackInfo info) {
        if (ItemPickupCallback.EVENT.invoker().onItemPickup(playerEntity_1, (ItemEntity)(Object)this) != ActionResult.PASS) {
            info.cancel();
        }
    }
}
