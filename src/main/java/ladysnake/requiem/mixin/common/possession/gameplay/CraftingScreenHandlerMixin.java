package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void allowSupercrafters(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            Entity possessedEntity = PossessionComponent.get(player).getPossessedEntity();
            if (possessedEntity != null && RequiemEntityTypeTags.SUPERCRAFTERS.contains(possessedEntity.getType())) {
                cir.setReturnValue(true);
            }
        }
    }
}
