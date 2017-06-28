package ladysnake.dissolution.common.entity;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import net.minecraft.block.Block;
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

public class EntityMinionSkeleton extends EntityMinion {
	
	protected static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.<Boolean>createKey(EntityMinionSkeleton.class, DataSerializers.BOOLEAN);
	protected boolean isStray;

	public EntityMinionSkeleton(World worldIn) {
		super(worldIn);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
	}
	
	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
		this.applyEntityAI();
	}
	
	protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(SWINGING_ARMS, Boolean.valueOf(false));
    }
	
	@Override
	protected SoundEvent getAmbientSound()
    {
        return (isCorpse()) ? null : (this instanceof EntityMinionStray) ? SoundEvents.ENTITY_STRAY_AMBIENT : SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound()
    {
    	return (isCorpse()) ? null : (this instanceof EntityMinionStray) ? SoundEvents.ENTITY_STRAY_HURT : SoundEvents.ENTITY_SKELETON_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return (isCorpse()) ? null : (this instanceof EntityMinionStray) ? SoundEvents.ENTITY_STRAY_DEATH : SoundEvents.ENTITY_SKELETON_DEATH;
    }

    protected SoundEvent getStepSound()
    {
    	return (isCorpse()) ? null : (this instanceof EntityMinionStray) ? SoundEvents.ENTITY_STRAY_STEP : SoundEvents.ENTITY_SKELETON_STEP;
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
    
	/*
	@Override
	public boolean isHandActive() {
		return true;
	}
	
	@Override
	public int getItemInUseMaxCount() {
		return 30;
	}
	*/
	@SideOnly(Side.CLIENT)
    public boolean isSwingingArms()
    {
        return ((Boolean)this.dataManager.get(SWINGING_ARMS)).booleanValue();
    }

    public void setSwingingArms(boolean swingingArms)
    {
        this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(swingingArms));
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
    	super.readFromNBT(compound);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	return super.writeToNBT(compound);
    }

}
