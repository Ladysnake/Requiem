package ladysnake.requiem.mixin.command;

import ladysnake.requiem.api.v1.internal.StatusEffectReapplicator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(EffectCommand.class)
public abstract class EffectCommandMixin {
    // ModifyVariable to capture the entity more easily
    @ModifyVariable(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z", shift = At.Shift.AFTER))
    private static Entity actuallyClearSoulboundEffects(Entity entity) {
        if (entity instanceof StatusEffectReapplicator) {
            ((StatusEffectReapplicator) entity).getReappliedStatusEffects().clear();
        }
        return entity;
    }

    // ModifyVariable to capture the entity more easily
    @ModifyVariable(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/entity/effect/StatusEffect;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tryRemoveStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", shift = At.Shift.AFTER))
    private static Entity actuallyClearSoulboundEffect(Entity entity, ServerCommandSource source, Collection<Entity> targets, StatusEffect effect) {
        if (entity instanceof StatusEffectReapplicator) {
            ((StatusEffectReapplicator) entity).getReappliedStatusEffects().removeIf(e -> e.getEffectType() == effect);
        }
        return entity;
    }
}
