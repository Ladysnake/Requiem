package ladysnake.requiem.mixin.entity;

import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "drop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"),
            cancellable = true
    )
    private void drop(DamageSource deathCause, CallbackInfo ci) {
        if (LivingEntityDropCallback.EVENT.invoker().onEntityDrop((LivingEntity)(Object)this, deathCause)) {
            ci.cancel();
        }
    }
}
