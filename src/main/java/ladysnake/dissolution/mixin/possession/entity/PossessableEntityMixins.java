package ladysnake.dissolution.mixin.possession.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityController;
import ladysnake.dissolution.api.v1.internal.ProtoPossessable;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.impl.ability.DummyMobAbilityController;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.BlockPos;
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

import static ladysnake.dissolution.common.impl.possession.entity.PossessableEntityImpl.INHERENT_MOB_SLOWNESS;
import static ladysnake.dissolution.common.impl.possession.entity.PossessableEntityImpl.INHERENT_MOB_SLOWNESS_UUID;

final class PossessableEntityMixins {
    private PossessableEntityMixins() {}

    @Mixin(Entity.class)
    private static abstract class EntityMixin implements ProtoPossessable {

        @Nullable
        @Override
        public PlayerEntity getPossessorEntity() {
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
        protected MobAbilityController abilityController = DummyMobAbilityController.DUMMY;

        public LivingEntityMixin(EntityType<?> type, World world) {
            super(type, world);
        }

        @Override
        public Optional<UUID> getPossessorUuid() {
            return getPossessor().map(PlayerEntity::getUuid);
        }

        @Override
        public Optional<PlayerEntity> getPossessor() {
            // method_18470 == getPlayerByUuid
            return Optional.ofNullable(possessor);
        }

        @Nullable
        @Override
        public PlayerEntity getPossessorEntity() {
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
            return this.abilityController;
        }

        @Override
        public void setPossessor(@CheckForNull PlayerEntity possessor) {
            if (this.getPossessor().map(p -> ((DissolutionPlayer)p).getPossessionComponent().getPossessedEntity()).filter(this::equals).isPresent()) {
                throw new IllegalStateException("Players must stop possessing an entity before it can change possessor!");
            }
            this.possessor = possessor;
            if (possessor != null) {
                this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addModifier(INHERENT_MOB_SLOWNESS);
            } else {
                this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(INHERENT_MOB_SLOWNESS_UUID);
            }
        }

        @Override
        public void onPossessorFalls(float fallDistance, double double_1, boolean boolean_1, BlockState blockState_1, BlockPos blockPos_1) {
            this.fallDistance = fallDistance;
            this.method_5623(double_1, boolean_1, blockState_1, blockPos_1);
        }
    }

    @Mixin({Entity.class, HorseBaseEntity.class, PigEntity.class, RavagerEntity.class})
    private static abstract class ControllableEntityMixin implements ProtoPossessable {
        @Inject(method = "getPrimaryPassenger", at = @At("HEAD"), cancellable = true)
        private void getPrimaryPassenger(CallbackInfoReturnable<Entity> cir) {
            PlayerEntity possessor = this.getPossessorEntity();
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
