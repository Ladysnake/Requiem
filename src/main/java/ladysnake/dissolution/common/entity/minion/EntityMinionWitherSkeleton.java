package ladysnake.dissolution.common.entity.minion;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityMinionWitherSkeleton extends EntityMinionSkeleton {
	
	public EntityMinionWitherSkeleton(World worldIn) {
		super(worldIn);
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
	
	@Override
	protected SoundEvent getAmbientSound() {
        return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound() {
    	return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_HURT;
    }

    protected SoundEvent getDeathSound() {
    	return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_DEATH;
    }

    protected SoundEvent getStepSound() {
    	return (isInert()) ? null : SoundEvents.ENTITY_WITHER_SKELETON_STEP;
    }
    
    @Override
    public boolean attackEntityAsMob(@Nonnull Entity entityIn)
    {
        if (!super.attackEntityAsMob(entityIn))
        {
            return false;
        }
        else
        {
            if (entityIn instanceof EntityLivingBase)
            {
                ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
            }

            return true;
        }
    }
    
    @Override
    protected void handleSunExposure() {}

}
