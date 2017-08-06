package ladysnake.dissolution.common.entity.minion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class AbstractMinion extends EntityCreature implements IEntityAdditionalSpawnData, IRangedAttackMob {
	protected boolean inert;
	protected int remainingTicks;
	public static final List<Class<? extends EntityCreature>> TARGET_BLACKLIST = new ArrayList();
	public static final int MAX_DEAD_TICKS = 1200;
	public static final int MAX_RISEN_TICKS = 6000;
	public static final int SUN_TICKS_PENALTY = 9;
	protected static float sizeX = 0.6F, sizeY = 1.95F;
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(AbstractMinion.class, DataSerializers.BOOLEAN);
    private final EntityAIMinionRangedAttack aiArrowAttack = new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false);
    
    static {
    	TARGET_BLACKLIST.add(EntityWanderingSoul.class);
    	if(!DissolutionConfig.entities.minionsAttackCreepers)
    		TARGET_BLACKLIST.add(EntityCreeper.class);
    }
	
	public AbstractMinion(World worldIn) {
		this(worldIn, false);
	}
	
	public AbstractMinion(World worldIn, boolean isChild) {
		super(worldIn);
        this.setSize(sizeX, sizeY);
        this.inert = true;
        this.remainingTicks = this.getMaxTimeRemaining();
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
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMob.class, 10, true, false, e -> !TARGET_BLACKLIST.contains(e.getClass())));
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
	
	public void doParticle(EnumParticleTypes part, Entity entity, int amount){
		for(int i = 0; i < amount; i++){
			Random rand = new Random();
			double motionX = rand.nextGaussian() * 0.1D;
			double motionY = rand.nextGaussian() * 0.1D;
			double motionZ = rand.nextGaussian() * 0.1D;
			entity.world.spawnParticle(part, false, entity.posX , entity.posY+ 1.0D, entity.posZ, motionX, motionY, motionZ, new int[0]);
		}
	}
	
	/**
     * Applies the given player interaction to this Entity.
     */
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand)
    {
    	
    	if(CapabilityIncorporealHandler.getHandler(player).isIncorporeal() && !player.isCreative())
    		return EnumActionResult.PASS;
    	
        ItemStack itemstack = player.getHeldItem(hand);

        if (itemstack.getItem() != Items.NAME_TAG && itemstack.getItem() != ModItems.EYE_OF_THE_UNDEAD)
        {
            if (!this.world.isRemote && !player.isSpectator())
            {
                EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

                if (itemstack.isEmpty())
                {
                    EntityEquipmentSlot entityequipmentslot2 = this.getClickedSlot(vec);

                    if (this.hasItemInSlot(entityequipmentslot2))
                    {
                        this.swapItem(player, entityequipmentslot2, itemstack, hand);
                    }
                    else 
                    {
                    	return EnumActionResult.PASS;
                    }
                }
                else
                {

                    this.swapItem(player, entityequipmentslot, itemstack, hand);
                }

                return EnumActionResult.SUCCESS;
            }
            else
            {
                return itemstack.isEmpty() && !this.hasItemInSlot(this.getClickedSlot(vec)) ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
            }
        }
        else
        {
            return EnumActionResult.PASS;
        }
    }

    
    /**
     * Vanilla code from the armor stand
     * @param raytrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EntityEquipmentSlot getClickedSlot(Vec3d raytrace)
    {
        EntityEquipmentSlot entityequipmentslot = EntityEquipmentSlot.MAINHAND;
        boolean flag = this.isChild();
        double d0 = (this.isCorpse() ? raytrace.z + 1.2 : raytrace.y) * (flag ? 2.0D : 1.0D);
        EntityEquipmentSlot entityequipmentslot1 = EntityEquipmentSlot.FEET;

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(entityequipmentslot1))
        {
            entityequipmentslot = EntityEquipmentSlot.FEET;
        }
        else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D) && this.hasItemInSlot(EntityEquipmentSlot.CHEST))
        {
            entityequipmentslot = EntityEquipmentSlot.CHEST;
        }
        else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EntityEquipmentSlot.LEGS))
        {
            entityequipmentslot = EntityEquipmentSlot.LEGS;
        }
        else if (d0 >= 1.6D && this.hasItemInSlot(EntityEquipmentSlot.HEAD))
        {
            entityequipmentslot = EntityEquipmentSlot.HEAD;
        }

        return entityequipmentslot;
    }
    
    private void swapItem(EntityPlayer player, EntityEquipmentSlot targetedSlot, ItemStack playerItemStack, EnumHand hand)
    {
        ItemStack itemstack = this.getItemStackFromSlot(targetedSlot);

//        if (itemstack.isEmpty() || (this.disabledSlots & 1 << p_184795_2_.getSlotIndex() + 8) == 0)
        {
//            if (!itemstack.isEmpty() || (this.disabledSlots & 1 << p_184795_2_.getSlotIndex() + 16) == 0)
            {
                if (player.capabilities.isCreativeMode && itemstack.isEmpty() && !playerItemStack.isEmpty())
                {
                    ItemStack itemstack2 = playerItemStack.copy();
                    itemstack2.setCount(1);
                    this.setItemStackToSlot(targetedSlot, itemstack2);
                }
                else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1)
                {
                    if (itemstack.isEmpty())
                    {
                        ItemStack itemstack1 = playerItemStack.copy();
                        itemstack1.setCount(1);
                        this.setItemStackToSlot(targetedSlot, itemstack1);
                        playerItemStack.shrink(1);
                    }
                }
                else
                {
                    this.setItemStackToSlot(targetedSlot, playerItemStack);
                    player.setHeldItem(hand, itemstack);
                }
            }
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
		super.onLivingUpdate();
		this.updateMinion();
	}
	
	protected void updateMinion() {
		remainingTicks--;
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
		}
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
			
			if (source.getTrueSource() instanceof EntityPlayer || source.canHarmInCreative()){
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
		this.inert = isCorpse;
		this.remainingTicks = getMaxTimeRemaining();
		
		if(isCorpse)
			this.setSize(sizeY, sizeX);
		else
			this.setSize(sizeX, sizeY);
	}
	
	public int getMaxTimeRemaining() {
		return this.inert ? MAX_DEAD_TICKS : MAX_RISEN_TICKS;
	}
	
	public boolean isCorpse(){
		return inert;
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
		this.inert = compound.getBoolean("Corpse");
		this.remainingTicks = compound.getInteger("remainingTicks");
		this.setChild(compound.getBoolean("IsBaby"));
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(inert);
		buffer.writeInt(remainingTicks);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		setCorpse(additionalData.readBoolean());
		remainingTicks = additionalData.readInt();
	}
	
	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
			ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
			if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack))
            {
                if (itemstack.isItemStackDamageable())
                {
                    itemstack.setItemDamage(this.rand.nextInt(Math.min(itemstack.getMaxDamage() / 10, 50)));
                }

                this.entityDropItem(itemstack, 0.0F);
            }

		}
	}
	
}
