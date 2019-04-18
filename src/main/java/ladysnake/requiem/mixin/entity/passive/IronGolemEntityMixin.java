package ladysnake.requiem.mixin.entity.passive;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolemEntity.class)
public abstract class IronGolemEntityMixin extends GolemEntity {
    protected IronGolemEntityMixin(EntityType<? extends GolemEntity> type, World world) {
        super(type, world);
    }

    /**
     * Make golem attacks correctly knock back possessed entities.
     * <p>
     * This could theoretically be replaced by a generic patch to make any
     * velocity change in the possessed entity affect its player, but given
     * the small amount of external changes to velocity and all the pitfalls
     * associated with a generic approach, special casing is currently preferred.
     */
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void knockbackPossessor(Entity target, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity possessor = ((ProtoPossessable)target).getPossessor();
        if (possessor != null) {
            possessor.setVelocity(possessor.getVelocity().add(0.0D, 0.4D, 0.0D));
        }
    }
}
