package ladysnake.dissolution.mixin.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.TriggerableAttacker;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements TriggerableAttacker {
    public LivingEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Shadow public abstract boolean method_6121(Entity entity_1);

    @Override
    public boolean triggerDirectAttack(PlayerEntity player, Entity target) {
        boolean success = this.method_6121(target);
        if (success && target instanceof LivingEntity) {
            player.getMainHandStack().onEntityDamaged((LivingEntity) target, player);
        }
        return success;
    }

    /**
     * Marks possessed entities as the attacker for any damage caused by their possessor
     *
     * @param source damage dealt
     * @param amount amount of damage dealt
     * @param info   callback
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void proxyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source.getAttacker() instanceof DissolutionPlayer) {
            Entity possessed = (Entity) ((DissolutionPlayer) source.getAttacker()).getPossessionManager().getPossessedEntity();
            if (possessed != null) {
                DamageSource newSource = null;
                if (source instanceof ProjectileDamageSource)
                    newSource = new ProjectileDamageSource(source.getName(), source.getSource(), possessed);
                else if (source instanceof EntityDamageSource)
                    newSource = new EntityDamageSource(source.getName(), possessed);
                if (newSource != null) {
                    ((LivingEntity) (Object) this).damage(newSource, amount);
                    info.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "canClimb", at = @At("HEAD"), cancellable = true)
    public void canClimb(CallbackInfoReturnable<Boolean> info) {
        if (this instanceof DissolutionPlayer && this.horizontalCollision) {
            LivingEntity possessed = (LivingEntity) ((DissolutionPlayer) this).getPossessionManager().getPossessedEntity();
            if (possessed != null) {
                info.setReturnValue(DissolutionEntityTags.CLIMBER.contains(possessed.getType()));
            }
        }
    }
}
