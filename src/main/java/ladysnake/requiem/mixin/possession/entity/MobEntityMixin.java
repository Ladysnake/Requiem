package ladysnake.requiem.mixin.possession.entity;

import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.entity.ai.InertGoal;
import ladysnake.requiem.common.impl.ability.ImmutableMobAbilityController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Possessable {
    @Shadow
    @Final
    protected GoalSelector goalSelector;

    private MobAbilityController abilityController = MobAbilityController.DUMMY;

    public MobEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAbilities(CallbackInfo ci) {
        if (world != null && !world.isClient) {
            this.goalSelector.add(99, new InertGoal(this));
            this.abilityController = new ImmutableMobAbilityController<>(RequiemRegistries.ABILITIES.getConfig((MobEntity)(Object)this), (MobEntity & Possessable)(Object)this);
        }
    }

    @Override
    public MobAbilityController getMobAbilityController() {
        return abilityController;
    }

    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void getEquippedStack(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getEquippedStack(slot));
        }
    }

    @Inject(method = "setEquippedStack", at = @At("HEAD"), cancellable = true)
    private void setEquippedStack(EquipmentSlot slot, ItemStack item, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null && !world.isClient) {
            possessor.setEquippedStack(slot, item);
        }
    }
}
