package ladysnake.dissolution.mixin.entity.passive;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(HorseBaseEntity.class)
public abstract class HorseBaseEntityMixin extends MobEntity {

    protected HorseBaseEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Shadow @Nullable public abstract Entity getPrimaryPassenger();

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    private void undeadHorsesAutoSaddled(CallbackInfoReturnable<Boolean> cir) {
        Entity passenger = this.getPrimaryPassenger();
        if (passenger instanceof DissolutionPlayer && this.isUndead()) {
            Possessable possessedEntity = ((DissolutionPlayer) passenger).getPossessionComponent().getPossessedEntity();
            if (possessedEntity instanceof LivingEntity && ((LivingEntity) possessedEntity).isUndead()) {
                cir.setReturnValue(true);
            }
        }
    }
}
