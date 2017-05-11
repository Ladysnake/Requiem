package ladysnake.dissolution.common.entity;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityMinion extends EntityCreature implements IEntityAdditionalSpawnData {
	public boolean corpse;
	protected int remainingTicks;
	public static int maxTicks = 1200;
	protected static float sizeX = 0.6F, sizeY = 1.95F;
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityMinion.class, DataSerializers.BOOLEAN);
	
	public EntityMinion(World worldIn) {
		super(worldIn);
        setSize(sizeX, sizeY);
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
	
    protected void entityInit()
    {
        super.entityInit();
        this.getDataManager().register(IS_CHILD, Boolean.valueOf(false));
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
	
	public void setChild(boolean childMinion)
    {
        this.getDataManager().set(IS_CHILD, Boolean.valueOf(childMinion));
        /*
        if (this.world != null && !this.world.isRemote)
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            //iattributeinstance.removeModifier(BABY_SPEED_BOOST);

            if (childMinion)
            {
            //    iattributeinstance.applyModifier(BABY_SPEED_BOOST);
            }
        }*/

        this.setChildSize(childMinion);
    }
	
	@Override
	public boolean isChild()
    {
        return ((Boolean)this.getDataManager().get(IS_CHILD)).booleanValue();
    }
	
	public void setChildSize(boolean isChild)
    {
		float ratio = (isChild ? 0.5F : 1.0F);
		if(isCorpse())
			super.setSize(sizeY*ratio, sizeX*ratio);
		else
			super.setSize(sizeX*ratio, sizeY*ratio);
    }
	
	public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (IS_CHILD.equals(key))
        {
            this.setChildSize(this.isChild());
        }

        super.notifyDataManagerChange(key);
    }
	
	@Override
	protected boolean canEquipItem(ItemStack stack) {
		return true;
	}
	
	@Override
	public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

	public void setCorpse(boolean isCorpse) {
		this.corpse = isCorpse;
		this.remainingTicks = maxTicks;
		
		if(isCorpse)
			this.setSize(sizeY, sizeX);
		else
			this.setSize(sizeX, sizeY);
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
		if (this.isChild())
            compound.setBoolean("IsBaby", true);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.corpse = compound.getBoolean("Corpse");
		this.setChild(compound.getBoolean("IsBaby"));
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(corpse);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		setCorpse(additionalData.readBoolean());
	}
	
}
