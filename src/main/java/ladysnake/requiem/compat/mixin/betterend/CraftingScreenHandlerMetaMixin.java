package ladysnake.requiem.compat.mixin.betterend;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMetaMixin {
    // BetterEnd does an unconditional inject+return, which is bad and breaks everything
    @Inject(method = {"handler$zic000$canUse", "handler$zid000$canUse", "handler$zie000$canUse"}, at = @At("RETURN"))
    private void sorry(PlayerEntity player, CallbackInfoReturnable<Boolean> cir, CallbackInfo ci) {
        if (!cir.getReturnValueZ()) {
            Entity possessedEntity = PossessionComponent.get(player).getPossessedEntity();
            if (possessedEntity != null && RequiemEntityTypeTags.SUPERCRAFTERS.contains(possessedEntity.getType())) {
                cir.setReturnValue(true);
            }
        }
    }
}
