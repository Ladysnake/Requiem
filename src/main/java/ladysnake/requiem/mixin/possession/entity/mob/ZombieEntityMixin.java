package ladysnake.requiem.mixin.possession.entity.mob;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin implements Possessable {
    @Inject(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;remove()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void possessConvertedZombie(EntityType<? extends ZombieEntity> type, CallbackInfo ci, ZombieEntity converted) {
        RequiemPlayer possessor = (RequiemPlayer) this.getPossessor();
        if (possessor != null) {
            PossessionComponent possessionComponent = possessor.getPossessionComponent();
            possessionComponent.stopPossessing(false);
            possessionComponent.startPossessing(converted);
        }
    }
}
