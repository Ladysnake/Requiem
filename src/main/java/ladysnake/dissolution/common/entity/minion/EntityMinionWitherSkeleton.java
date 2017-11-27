package ladysnake.dissolution.common.entity.minion;

import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityMinionWitherSkeleton extends EntityMinionSkeleton {

    public EntityMinionWitherSkeleton(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true;
        this.setSize(0.7F, 2.4F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMinionAttack(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.applyEntityAI();
    }

    protected void entityInit() {
        super.entityInit();
    }

    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData ientitylivingdata = super.onInitialSpawn(difficulty, livingdata);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.setCombatTask();
        return ientitylivingdata;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
    }

    protected SoundEvent getStepSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_STEP;
    }

    @Override
    public boolean attackEntityAsMob(@Nonnull Entity entityIn) {
        if (!super.attackEntityAsMob(entityIn)) {
            return false;
        } else {
            if (entityIn instanceof EntityLivingBase) {
                ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
            }

            return true;
        }
    }

    @Override
    protected EntityArrow getArrow(EntityTippedArrow baseArrow, float distanceFactor) {
        EntityArrow entityarrow = super.getArrow(baseArrow, distanceFactor);
        entityarrow.setFire(100);
        return entityarrow;
    }

    @Override
    public float getEyeHeight() {
        return 2.1F;
    }

    @Override
    protected void handleSunExposure() {
    }

}
