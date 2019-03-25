package ladysnake.dissolution.mixin.client.render.entity;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @SuppressWarnings("UnresolvedMixinReference")   // Minecraft dev plugin is an idiot sandwich
    @Nullable
    @Redirect(method = "method_4054", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRiddenEntity()Lnet/minecraft/entity/Entity;"))
    private Entity getPossessorRiddenEntity(LivingEntity entity) {
        PlayerEntity possessor = ((Possessable) entity).getPossessor();
        if (possessor != null) {
            return possessor.getRiddenEntity();
        }
        return entity.getRiddenEntity();
    }

    @SuppressWarnings("UnresolvedMixinReference")   // Minecraft dev plugin is an idiot sandwich
    @Redirect(method = "method_4054", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasVehicle()Z"))
    private boolean doesPossessorHaveVehicle(LivingEntity entity) {
        PlayerEntity possessor = ((Possessable) entity).getPossessor();
        if (possessor != null) {
            return possessor.hasVehicle();
        }
        return entity.hasVehicle();
    }
}
