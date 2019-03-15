package ladysnake.dissolution.mixin.possession.entity;

import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.common.DissolutionRegistries;
import ladysnake.dissolution.common.entity.ai.InertGoal;
import ladysnake.dissolution.common.impl.ability.ImmutableMobAbilityController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goals;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends PossessableEntityMixins.LivingEntityMixin implements Possessable {
    @Shadow
    @Final
    protected Goals goalSelector;

    public MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initGoals", at = @At("RETURN"))
    private void initAbilities(CallbackInfo ci) {
        this.goalSelector.add(99, new InertGoal(this));
        this.abilityController = new ImmutableMobAbilityController<>(DissolutionRegistries.ABILITIES.getConfig((MobEntity)(Object)this), (MobEntity & Possessable)(Object)this);
    }
}
