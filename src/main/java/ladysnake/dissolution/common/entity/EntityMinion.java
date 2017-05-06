package ladysnake.dissolution.common.entity;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class EntityMinion extends EntityCreature {
	public boolean corpse;
	protected int remainingTicks;
	public static int maxTicks = 1200;
	
	public EntityMinion(World worldIn) {
		super(worldIn);
        setSize(0.6F, 1.95F);
        corpse = true;
        this.remainingTicks = maxTicks;
	}
	
	@Override
	protected abstract void initEntityAI();
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.isAIDisabled();
	}
	
	@Override
	public boolean isAIDisabled() {
		if(!isCorpse()){
			return false;
		}
		return true;
	}
	
	public void DoParticle(EnumParticleTypes part, Entity entity, int amount){
		for(int i = 0; i < amount; i++){
			Random rand = new Random();
			double motionX = rand.nextGaussian() * 0.1D;
			double motionY = rand.nextGaussian() * 0.1D;
			double motionZ = rand.nextGaussian() * 0.1D;
			entity.world.spawnParticle(part, false, entity.posX , entity.posY+ 1.0D, entity.posZ, motionX, motionY, motionZ, new int[0]);
		}
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

        if (flag)
        {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }
	
	@Override
	public void onUpdate() {
		if(this.isCorpse()){
			remainingTicks--;
			if(remainingTicks <= 0){
				this.setDead();
				return;
			}
		}	
		super.onUpdate();
	}
	
	@Override
	protected boolean canEquipItem(ItemStack stack) {
		return true;
	}

	public void setCorpse(boolean isCorpse) {
		this.corpse = isCorpse;
		this.remainingTicks = maxTicks;
	}
	
	public boolean isCorpse(){
		return corpse;
	}
	
	public int getRemainingTicks() {
		return remainingTicks;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("Corpse", this.isCorpse());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.corpse = compound.getBoolean("Corpse");
	}
	
}
