package ladysnake.dissolution.common.entity;

import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityMinion extends EntityCreature implements IEntityAdditionalSpawnData, IRangedAttackMob {
	public boolean corpse;
	protected int remainingTicks;
	public static final int MAX_DEAD_TICKS = 1200;
	public static final int MAX_RISEN_TICKS = 6000;
	public static final int SUN_TICKS_PENALTY = 9;
	protected static float sizeX = 0.6F, sizeY = 1.95F;
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityMinion.class, DataSerializers.BOOLEAN);
    private final EntityAIMinionRangedAttack aiArrowAttack = new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false);
	
	public EntityMinion(World worldIn) {
		this(worldIn, false);
	}
	
	public EntityMinion(World worldIn, boolean isChild) {
		super(worldIn);
        this.setSize(sizeX, sizeY);
        this.corpse = true;
        this.remainingTicks = MAX_DEAD_TICKS;
		this.setChild(isChild);
	}
	
	@Override
	protected abstract void initEntityAI();

	protected void applyEntityAI() {
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[] { EntityPigZombie.class }) {
			@Override
			protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
				return super.isSuitableTarget(target, includeInvincibles) && !(target instanceof EntityPlayer);
			}
		});
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMob.class, true));
	}
	
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
	
	public void setCombatTask()
    {
        if (this.world != null && !this.world.isRemote)
        {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() == Items.BOW)
            {
                int i = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD)
                {
                    i = 40;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(4, this.aiArrowAttack);
            }
            else
            {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		EntityArrow entityarrow = this.getArrow(distanceFactor);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
	}
	
	protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        //entitytippedarrow.setEnchantmentEffectsFromEntity(this, p_190726_1_);
        if(this instanceof EntityMinionStray)
        	entitytippedarrow.addEffect(new PotionEffect(MobEffects.SLOWNESS, 600));
        return entitytippedarrow;
    }
	
	@Override
	public void onLivingUpdate() {
		//if(this.isCorpse()){
			remainingTicks--;
			//System.out.println(remainingTicks);
			if(!this.isCorpse())
				this.handleSunExposition();
			if(remainingTicks <= 0){
				if(!this.isCorpse() && !this.world.isRemote) {
					for(int i = 0; i < 150; i++) {
					    double motionX = rand.nextGaussian() * 0.05D;
					    double motionY = rand.nextGaussian() * 0.05D;
					    double motionZ = rand.nextGaussian() * 0.05D;
					    ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false, posX, posY + 1.5D, posZ, 1, 0.3D, 0.3D, 0.3D, 0.0D, new int[0]); 
					}
				}
				this.setDead();
				return;
			}
		//}	
		super.onLivingUpdate();
	}
	
	protected void handleSunExposition() {
		if(this.world.isDaytime() && !this.world.isRemote && 
				this.world.canSeeSky(new BlockPos(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ)) &&
				!this.world.isRaining()) {
			boolean flag = true;
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

            if (!itemstack.isEmpty())
            {
                if (itemstack.isItemStackDamageable())
                {
                    itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));

                    if (itemstack.getItemDamage() >= itemstack.getMaxDamage())
                    {
                        this.renderBrokenItemStack(itemstack);
                        this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }

                flag = false;
            }

            if (flag && !this.isBurning())
            {
            	this.setFire(1);
            }
		}
		if(this.isBurning()) {
            this.remainingTicks -= (isCorpse()) ? SUN_TICKS_PENALTY / 2.0f : SUN_TICKS_PENALTY * 2;
//           	System.out.println((this.world.isRemote ? "client: " : "server: ") + this.remainingTicks);
		}
	}
	
	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		super.damageEntity(damageSrc, damageAmount);
		if(!damageSrc.isFireDamage())
			this.remainingTicks -= 5;
	}
	
	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if(this.isCorpse()) {
			if(super.isEntityInvulnerable(source))
				return true;
			
			if (source.getEntity() instanceof EntityPlayer || source.canHarmInCreative()){
				return false;
			}
		    return true;
		}
		
		if(source.isFireDamage())
			return rand.nextBoolean();
		return super.isEntityInvulnerable(source);
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
		this.remainingTicks = isCorpse ? MAX_DEAD_TICKS : MAX_RISEN_TICKS;
		
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
		compound.setInteger("remainingTicks", remainingTicks);
		if (this.isChild())
            compound.setBoolean("IsBaby", true);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.corpse = compound.getBoolean("Corpse");
		this.remainingTicks = compound.getInteger("remainingTicks");
		this.setChild(compound.getBoolean("IsBaby"));
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(corpse);
		buffer.writeInt(remainingTicks);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		setCorpse(additionalData.readBoolean());
		remainingTicks = additionalData.readInt();
	}
	
}
