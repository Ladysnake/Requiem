package ladysnake.requiem.mixin.entity.mob;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.thrown.ThrownPotionEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;
import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin {
    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/thrown/ThrownPotionEntity;setItemStack(Lnet/minecraft/item/ItemStack;)V", shift = AFTER),
            locals = CAPTURE_FAILSOFT
    )
    private void makeWitchesThrowWeaknessAtUndead(
            LivingEntity target,
            float charge,
            CallbackInfo ci,
            Vec3d targetVelocity,
            double dx,
            double dy,
            double dz,
            float horizontalDistance,
            Potion potion,
            ThrownPotionEntity thrownPotion
    ) {
        if (target.isUndead() && !target.hasStatusEffect(StatusEffects.WEAKNESS)) {
            PotionUtil.setPotion(thrownPotion.getStack(), Potions.WEAKNESS);
        }
    }
}
