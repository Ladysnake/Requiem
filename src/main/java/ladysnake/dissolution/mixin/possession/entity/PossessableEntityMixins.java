package ladysnake.dissolution.mixin.possession.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.annotation.CalledThroughReflection;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityController;
import ladysnake.dissolution.api.v1.internal.ProtoPossessable;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.VanillaDissolutionPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@CalledThroughReflection
public final class PossessableEntityMixins {

    private PossessableEntityMixins() {}

    @Mixin(Entity.class)
    private static abstract class EntityMixin implements ProtoPossessable {

        @Nullable
        @Override
        public PlayerEntity getPossessor() {
            return null;
        }

        @Override
        public boolean isBeingPossessed() {
            return false;
        }
    }

    @Mixin(LivingEntity.class)
    static abstract class LivingEntityMixin extends Entity implements Possessable {
        @Shadow public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute entityAttribute_1);

        @Nullable
        private PlayerEntity possessor;

        public LivingEntityMixin(EntityType<?> type, World world) {
            super(type, world);
        }

        @Override
        public Optional<UUID> getPossessorUuid() {
            return Optional.ofNullable(this.possessor).map(PlayerEntity::getUuid);
        }

        @Override
        public boolean isBeingPossessed() {
            return this.possessor != null;
        }

        @Nullable
        @Override
        public PlayerEntity getPossessor() {
            if (this.possessor != null && this.possessor.invalid) {
                ((DissolutionPlayer)this.possessor).getPossessionComponent().stopPossessing();
                // Make doubly sure
                this.setPossessor(null);
            }
            return possessor;
        }

        @Override
        public boolean canBePossessedBy(PlayerEntity player) {
            return !this.isBeingPossessed();
        }

        @Override
        public MobAbilityController getMobAbilityController() {
            return MobAbilityController.DUMMY;
        }

        @Override
        public void setPossessor(@CheckForNull PlayerEntity possessor) {
            if (this.possessor != null && ((DissolutionPlayer) this.possessor).getPossessionComponent().getPossessedEntity() == this) {
                throw new IllegalStateException("Players must stop possessing an entity before it can change possessor!");
            }
            this.possessor = possessor;
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(VanillaDissolutionPlugin.INHERENT_MOB_SLOWNESS_UUID);
            if (possessor != null) {
                this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addModifier(VanillaDissolutionPlugin.INHERENT_MOB_SLOWNESS);
            }
        }

    }

    @Mixin({Entity.class, HorseBaseEntity.class, PigEntity.class, RavagerEntity.class})
    private static abstract class ControllableEntityMixin implements ProtoPossessable {
        @Inject(method = "getPrimaryPassenger", at = @At("HEAD"), cancellable = true)
        private void getPrimaryPassenger(CallbackInfoReturnable<Entity> cir) {
            PlayerEntity possessor = this.getPossessor();
            if (possessor != null) {
                cir.setReturnValue(possessor);
            }
        }
    }

    @Mixin({MobEntity.class, HorseBaseEntity.class, LlamaEntity.class, PigEntity.class, RavagerEntity.class})
    private static abstract class ControllableMobEntityMixin extends LivingEntity implements Possessable{

        protected ControllableMobEntityMixin(EntityType<? extends LivingEntity> type, World world) {
            super(type, world);
        }

        @Inject(method = "method_5956", at = @At("HEAD"), cancellable = true)
        private void canRiderMove(CallbackInfoReturnable<Boolean> cir) {
            if (this.isBeingPossessed()) {
                cir.setReturnValue(true);
            }
        }
    }
}
