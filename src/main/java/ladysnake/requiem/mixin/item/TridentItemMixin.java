package ladysnake.requiem.mixin.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item {
    private static ThreadLocal<Boolean> REVERT_CREATIVE_MODE = ThreadLocal.withInitial(() -> false);

    public TridentItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onItemStopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;method_7474(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private void giveDrownedInfinity(ItemStack stack, World world, LivingEntity user, int ticks, CallbackInfo ci) {
        if (((RequiemPlayer)user).getPossessionComponent().getPossessedEntity() instanceof DrownedEntity && random.nextFloat() < 0.8f) {
            PlayerAbilities abilities = ((PlayerEntity) user).abilities;
            if (!abilities.creativeMode) {
                // Makes the trident not consume the item
                abilities.creativeMode = true;
                REVERT_CREATIVE_MODE.set(true);
            }
        }
    }

    @Inject(method = "onItemStopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"))
    private void revertCreativeMode(ItemStack stack, World world, LivingEntity user, int ticks, CallbackInfo ci) {
        if (REVERT_CREATIVE_MODE.get()) {
            ((PlayerEntity)user).abilities.creativeMode = false;
            REVERT_CREATIVE_MODE.set(false);
        }
    }
}
