package ladysnake.dissolution.common.entity;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMinionZombie extends EntityMinion {

	private boolean isHusk;

	public EntityMinionZombie(World worldIn) {
		this(worldIn, false, false);
	}

	/**
	 * Creates a zombie minion
	 * 
	 * @param worldIn
	 * @param zombieType
	 *            false for the default zombie
	 */
	public EntityMinionZombie(World worldIn, boolean isHusk, boolean isChild) {
		super(worldIn, isChild);
		this.isHusk = isHusk;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIMinionAttack(this, 1.0D, false));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
		this.applyEntityAI();
	}
	
	public void setHusk(boolean husk) {
		this.isHusk = husk;
	}
	
	public boolean isHusk() {
		return isHusk;
	}
	
	@Override
	protected SoundEvent getAmbientSound()
    {
        return (isCorpse()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    protected SoundEvent getHurtSound()
    {
    	return (isCorpse()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return (isCorpse()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound()
    {
    	return (isCorpse()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_STEP;
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("isHusk", this.isHusk());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setHusk(compound.getBoolean("isHusk"));
	}
	
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);
		buffer.writeBoolean(isHusk());
	}
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		super.readSpawnData(additionalData);
		setHusk(additionalData.readBoolean());
	}

}
