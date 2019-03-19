package ladysnake.dissolution.mixin.entity.mob;

import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.util.ItemUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(WitchEntity.class)
public class WitchEntityMixin extends HostileEntity {
    protected WitchEntityMixin(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "isDrinking", at = @At("HEAD"), cancellable = true)
    private void isDrinking(CallbackInfoReturnable<Boolean> cir) {
        if (((Possessable)this).isBeingPossessed() && this.getMainHandStack().getItem() != Items.POTION) {
            cir.setReturnValue(false);
        }
    }

    @Nullable
    @ModifyVariable(method = "updateMovement", ordinal = 0, at = @At("STORE"))
    private Potion preventPotionOverride(final Potion selectedPotion) {
        if (((Possessable)this).isBeingPossessed() && !ItemUtil.isWaterBottle(this.getMainHandStack())) {
            return null;
        }
        return selectedPotion;
    }

    @Inject(
            method = "updateMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/WitchEntity;setEquippedStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V",
                    ordinal = 0,
                    shift = AFTER
            )
    )
    private void giveBottleBack(CallbackInfo ci) {
        if (((Possessable)this).isBeingPossessed()) {
            this.setEquippedStack(EquipmentSlot.HAND_MAIN, new ItemStack(Items.GLASS_BOTTLE));
        }
    }
}
