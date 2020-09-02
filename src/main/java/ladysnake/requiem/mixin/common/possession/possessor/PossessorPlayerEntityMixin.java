package ladysnake.requiem.mixin.common.possession.possessor;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import ladysnake.requiem.common.tag.RequiemItemTags;
import ladysnake.requiem.mixin.common.access.LivingEntityAccessor;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(PlayerEntity.class)
public abstract class PossessorPlayerEntityMixin extends LivingEntity {

    @Shadow
    public abstract HungerManager getHungerManager();

    protected PossessorPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travel(CallbackInfo info) {
        Entity possessed = PossessionComponent.getPossessedEntity(this);
        if (possessed != null && ((VariableMobilityEntity) possessed).requiem_isImmovable()) {
            if (!world.isClient && (this.getX() != possessed.getX() || this.getY() != possessed.getY() || this.getZ() != possessed.getZ())) {
                ServerPlayNetworkHandler networkHandler = ((ServerPlayerEntity) (Object) this).networkHandler;
                networkHandler.teleportRequest(possessed.getX(), possessed.getY(), possessed.getZ(), this.yaw, this.pitch, EnumSet.allOf(PlayerPositionLookS2CPacket.Flag.class));
                networkHandler.syncWithPlayerPosition();
            }
            info.cancel();
        }
    }

    /**
     * Players' sizes are hardcoded in an immutable enum map.
     * This injection delegates the call to the possessed entity, if any.
     */
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void adjustSize(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Entity possessedEntity = PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessedEntity != null) {
            cir.setReturnValue(possessedEntity.getDimensions(pose));
        }
    }

    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    private void canSoulConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        Possessable possessed = (Possessable) PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessed != null) {
            cir.setReturnValue(ignoreHunger || possessed.isRegularEater() && this.getHungerManager().isNotFull());
        }
    }

    @Inject(method = "canFoodHeal", at = @At("RETURN"), cancellable = true)
    private void canFoodHealPossessed(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity possessed = PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessed != null) {
            cir.setReturnValue(((Possessable) possessed).isRegularEater() && possessed.getHealth() > 0 && possessed.getHealth() < possessed.getMaxHealth());
        }
    }

    @Inject(method = "addExhaustion", slice = @Slice(to = @At("INVOKE:FIRST")), at = @At(value = "RETURN"))
    private void addExhaustion(float exhaustion, CallbackInfo ci) {
        Possessable possessed = (Possessable) PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessed != null && possessed.isRegularEater()) {
            if (!this.world.isClient) {
                this.getHungerManager().addExhaustion(exhaustion);
            }
        }
    }

    @Inject(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;eat(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)V"))
    private void eatZombieFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        MobEntity possessedEntity = PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessedEntity instanceof ZombieEntity && stack.getItem().isFood()) {
            if (RequiemItemTags.RAW_MEATS.contains(stack.getItem()) || RequiemItemTags.RAW_FISHES.contains(stack.getItem()) && possessedEntity instanceof DrownedEntity) {
                FoodComponent food = stack.getItem().getFoodComponent();
                assert food != null;
                possessedEntity.heal(food.getHunger());
            }
        }
        if (possessedEntity != null && possessedEntity.isUndead() && RequiemItemTags.UNDEAD_CURES.contains(stack.getItem()) && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            PossessionComponent.KEY.get(this).startCuring();
        }
    }

    @Intrinsic
    @Override
    public int getMaxAir() {
        return super.getMaxAir();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    private void delegateMaxBreath(CallbackInfoReturnable<Integer> cir) {
        // This method can be called in the constructor, before CCA is initialized
        if (ComponentProvider.fromEntity(this).getComponentContainer() != null) {
            Entity possessedEntity = PossessionComponent.getPossessedEntity(this);
            if (possessedEntity != null) {
                cir.setReturnValue(possessedEntity.getMaxAir());
            }
        }
    }

    @Intrinsic
    @Override
    public boolean isClimbing() {
        return super.isClimbing();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void canClimb(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.horizontalCollision) {
            cir.setReturnValue(MovementAlterer.KEY.get(this).canClimbWalls());
        }
    }

    @Intrinsic
    @Override
    public boolean collides() {
        return super.collides();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "collides", at = @At("RETURN"), cancellable = true)
    private void preventSoulsCollision(CallbackInfoReturnable<Boolean> info) {
        if (RemnantComponent.isSoul(this)) {
            info.setReturnValue(false);
        }
    }

    @Intrinsic
    @Override
    public boolean canAvoidTraps() {
        return super.canAvoidTraps();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "canAvoidTraps", at = @At("RETURN"), cancellable = true)
    private void soulsAvoidTraps(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || RemnantComponent.isIncorporeal(this));
    }

    @Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
    private void adjustEyeHeight(EntityPose pose, EntityDimensions size, CallbackInfoReturnable<Float> cir) {
        // This method can be called in the Entity constructor, before CCA is initialized
        if (ComponentProvider.fromEntity(this).getComponentContainer() != null) {
            LivingEntity possessed = PossessionComponent.getPossessedEntity(this);
            if (possessed != null) {
                cir.setReturnValue(((LivingEntityAccessor) possessed).invokeGetEyeHeight(pose, possessed.getDimensions(pose)));
            }
        }
    }
}
