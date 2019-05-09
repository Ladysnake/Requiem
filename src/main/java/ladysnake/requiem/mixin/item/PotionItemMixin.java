package ladysnake.requiem.mixin.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @ModifyArg(method = "onItemFinishedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V"))
    private LivingEntity targetPossessedEntity(LivingEntity entity) {
        if (entity instanceof RequiemPlayer) {
            LivingEntity possessed = ((RequiemPlayer) entity).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                return possessed;
            }
        }
        return entity;
    }
}
