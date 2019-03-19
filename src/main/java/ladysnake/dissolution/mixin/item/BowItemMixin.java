package ladysnake.dissolution.mixin.item;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.common.entity.internal.ArrowShooter;
import ladysnake.dissolution.common.entity.internal.ItemStackConvertible;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BaseBowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends BaseBowItem {
    private static final ThreadLocal<LivingEntity> DISSOLUTION$CURRENT_USER = new ThreadLocal<>();
    public BowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(
            method = "onItemStopUsing",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z",
                    ordinal = 0
            )
    )
    private void setCurrentUser(ItemStack item, World world, LivingEntity user, int charge, CallbackInfo ci) {
        DISSOLUTION$CURRENT_USER.set((LivingEntity)((DissolutionPlayer) user).getPossessionComponent().getPossessedEntity());
    }

    @ModifyVariable(method = "onItemStopUsing", ordinal = 0, at = @At("STORE"))
    private boolean giveSkeletonInfinity(boolean infinity) {
        if (DISSOLUTION$CURRENT_USER.get() instanceof AbstractSkeletonEntity) {
            return infinity || random.nextFloat() < 0.8f;
        }
        return infinity;
    }

    @ModifyVariable(method = "onItemStopUsing", ordinal = 0, at = @At("STORE"))
    private ProjectileEntity useSkeletonArrow(ProjectileEntity firedArrow) {
        LivingEntity entity = DISSOLUTION$CURRENT_USER.get();
        if (entity instanceof ArrowShooter) {
            return ((ArrowShooter)entity).invokeGetArrow(((ItemStackConvertible)firedArrow).invokeAsItemStack(), 1f);
        }
        return firedArrow;
    }
}
