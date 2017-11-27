package ladysnake.dissolution.common.entity.minion;

import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityMinionStray extends EntityMinionSkeleton {

    public EntityMinionStray(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F));
        this.tasks.addTask(3, new EntityAIMinionAttack(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.applyEntityAI();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_STRAY_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return (isInert()) ? null : SoundEvents.ENTITY_STRAY_HURT;
    }

    protected SoundEvent getDeathSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_STRAY_DEATH;
    }

    protected SoundEvent getStepSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_STRAY_STEP;
    }

    @Override
    protected EntityArrow getArrow(EntityTippedArrow baseArrow, float distanceFactor) {
        EntityTippedArrow arrow = (EntityTippedArrow) super.getArrow(baseArrow, distanceFactor);
        arrow.addEffect(new PotionEffect(MobEffects.SLOWNESS, 600));
        return arrow;
    }

}
