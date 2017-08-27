package ladysnake.dissolution.common.entity.minion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import ladysnake.dissolution.common.handlers.LivingDeathHandler;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
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

public abstract class AbstractMinion extends EntityCreature implements IRangedAttackMob, IEntityOwnable {
	public static final int MAX_DEAD_TICKS = 1200;
	public static final int MAX_RISEN_TICKS = 6000;
	public static final int SUN_TICKS_PENALTY = 15;
	public static final int DAMAGE_PENALTY = 5;

	protected static final float SIZE_X = 0.6F, SIZE_Y = 1.95F;
	
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.
			<Boolean>createKey(AbstractMinion.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> INERT = EntityDataManager.
			<Boolean>createKey(AbstractMinion.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> DECOMPOSITION_COUNTDOWN = EntityDataManager
			.<Integer>createKey(AbstractMinion.class, DataSerializers.VARINT);
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager
			.<Optional<UUID>>createKey(AbstractMinion.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	private final EntityAIMinionRangedAttack aiArrowAttack = new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F);
	private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false);
	
	public static AbstractMinion createMinion(EntityLivingBase deadGuy) {

		AbstractMinion corpse = null;
		
		if(deadGuy instanceof EntityPigZombie) {
			corpse = new EntityMinionPigZombie(deadGuy.world, ((EntityZombie)deadGuy).isChild());
		} else if (deadGuy instanceof EntityZombie) {
			corpse = new EntityMinionZombie(deadGuy.world, deadGuy instanceof EntityHusk, ((EntityZombie)deadGuy).isChild());
		} else if (deadGuy instanceof EntitySkeleton) {
			corpse = new EntityMinionSkeleton(deadGuy.world);
		} else if(deadGuy instanceof EntityStray){
			corpse = new EntityMinionStray(deadGuy.world);
		} else if(deadGuy instanceof EntityWitherSkeleton){
			corpse = new EntityMinionWitherSkeleton(deadGuy.world);
		}

		if (corpse != null) {
			corpse.setPosition(deadGuy.posX, deadGuy.posY, deadGuy.posZ);
			corpse.onUpdate();
		}
		
		return corpse;
	}

	public AbstractMinion(World worldIn) {
		this(worldIn, false);
	}

	public AbstractMinion(World worldIn, boolean isChild) {
		super(worldIn);
		this.setSize(SIZE_X, SIZE_Y);
		this.setChild(isChild);
	}

	@Override
	protected abstract void initEntityAI();

	protected void applyEntityAI() {
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true) {
			@Override
			protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
				return super.isSuitableTarget(target, includeInvincibles) && target != getOwner();
			}
		});
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMob.class, 10, true, false,
				e -> !DissolutionConfigManager.isEntityBlacklistedFromMinionAttacks((EntityMob)e)));
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

	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(IS_CHILD, false);
		this.getDataManager().register(INERT, true);
		this.getDataManager().register(DECOMPOSITION_COUNTDOWN, MAX_DEAD_TICKS);
		this.getDataManager().register(OWNER_UNIQUE_ID, Optional.absent());
	}

	public int getMaxTimeRemaining() {
		return this.isInert() ? MAX_DEAD_TICKS : MAX_RISEN_TICKS;
	}
	
	/**
	 * @return the time (in ticks) that this entity has before disappearing
	 */
	public int getRemainingTicks() {
		return this.getDataManager().get(DECOMPOSITION_COUNTDOWN);
	}

	/**
	 * Sets the time in ticks this entity has left
	 * @param countdown
	 */
	protected void setDecompositionCountdown(int countdown) {
		this.getDataManager().set(DECOMPOSITION_COUNTDOWN, countdown);
	}
	
	/**
	 * @param ticks the amount of ticks that will be deducted from the countdown
	 */
	protected void countdown(int ticks) {
		setDecompositionCountdown(getRemainingTicks() - ticks);
	}

	@Override
	public boolean isAIDisabled() {
		return isInert() || super.isAIDisabled();
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this),
				(float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

		if (flag) {
			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	public void setCombatTask() {
		if (this.world != null && !this.world.isRemote) {
			this.tasks.removeTask(this.aiAttackOnCollide);
			this.tasks.removeTask(this.aiArrowAttack);
			ItemStack itemstack = this.getHeldItemMainhand();

			if (itemstack.getItem() == Items.BOW) {
				int i = 20;

				if (this.world.getDifficulty() != EnumDifficulty.HARD) {
					i = 40;
				}

				this.aiArrowAttack.setAttackCooldown(i);
				this.tasks.addTask(4, this.aiArrowAttack);
			} else {
				this.tasks.addTask(4, this.aiAttackOnCollide);
			}
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		EntityArrow entityarrow = this.getArrow(distanceFactor);
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
		double d2 = target.posZ - this.posZ;
		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
		entityarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F,
				(float) (14 - this.world.getDifficulty().getDifficultyId() * 4));
		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(entityarrow);
	}

	protected EntityArrow getArrow(float distanceFactor) {
		EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
		return entitytippedarrow;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.updateMinion();
	}

	protected void updateMinion() {
		countdown(1);
		if (!this.isInert())
			this.handleSunExposure();
		if (getRemainingTicks() <= 0 && !this.world.isRemote) {
			if (!this.isInert()) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false, posX, posY + 1.5D, posZ,
						150, 0.3D, 0.3D, 0.3D, 0.0D);
			}
			this.setDead();
		}
	}

	protected void handleSunExposure() {
		if (this.world.isDaytime() && !this.world.isRemote
				&& this.world.canSeeSky(new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ))
				&& !this.world.isRaining()) {
			boolean flag = true;
			ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

			if (!itemstack.isEmpty()) {
				if (itemstack.isItemStackDamageable()) {
					itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));

					if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
						this.renderBrokenItemStack(itemstack);
						this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
					}
				}

				flag = false;
			}

			if (flag && !this.isBurning()) {
				this.setFire(1);
			}
		}
		if (this.isBurning()) {
			countdown(SUN_TICKS_PENALTY);
		}
	}

	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		super.damageEntity(damageSrc, damageAmount);
		this.countdown(DAMAGE_PENALTY);
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if (this.isInert() && !(source.getTrueSource() instanceof EntityPlayer || source.canHarmInCreative()))
				return true;

		return super.isEntityInvulnerable(source);
	}

	/**
	 * Applies the given player interaction to this Entity.
	 */
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {

		if (CapabilityIncorporealHandler.getHandler(player).isIncorporeal() && !player.isCreative())
			return EnumActionResult.PASS;

		ItemStack itemstack = player.getHeldItem(hand);

		if (itemstack.getItem() != Items.NAME_TAG && itemstack.getItem() != ModItems.EYE_OF_THE_UNDEAD) {
			if (!this.world.isRemote && !player.isSpectator()) {
				EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

				if (itemstack.isEmpty()) {
					EntityEquipmentSlot entityequipmentslot2 = this.getClickedSlot(vec);

					if (this.hasItemInSlot(entityequipmentslot2)) {
						this.swapItem(player, entityequipmentslot2, itemstack, hand);
					} else {
						return EnumActionResult.PASS;
					}
				} else {

					this.swapItem(player, entityequipmentslot, itemstack, hand);
				}

				return EnumActionResult.SUCCESS;
			} else {
				return itemstack.isEmpty() && !this.hasItemInSlot(this.getClickedSlot(vec)) ? EnumActionResult.PASS
						: EnumActionResult.SUCCESS;
			}
		} else {
			return EnumActionResult.PASS;
		}
	}

	/**
	 * Vanilla code from the armor stand
	 * 
	 * @param raytrace
	 *            the look vector of the player
	 * @return the targeted equipment slot
	 */
	protected EntityEquipmentSlot getClickedSlot(Vec3d raytrace) {
		EntityEquipmentSlot entityequipmentslot = EntityEquipmentSlot.MAINHAND;
		boolean flag = this.isChild();
		double d0 = (this.isInert() ? raytrace.z + 1.2 : raytrace.y) * (flag ? 2.0D : 1.0D);
		EntityEquipmentSlot entityequipmentslot1 = EntityEquipmentSlot.FEET;

		if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(entityequipmentslot1)) {
			entityequipmentslot = EntityEquipmentSlot.FEET;
		} else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
				&& this.hasItemInSlot(EntityEquipmentSlot.CHEST)) {
			entityequipmentslot = EntityEquipmentSlot.CHEST;
		} else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EntityEquipmentSlot.LEGS)) {
			entityequipmentslot = EntityEquipmentSlot.LEGS;
		} else if (d0 >= 1.6D && this.hasItemInSlot(EntityEquipmentSlot.HEAD)) {
			entityequipmentslot = EntityEquipmentSlot.HEAD;
		}

		return entityequipmentslot;
	}

	private void swapItem(EntityPlayer player, EntityEquipmentSlot targetedSlot, ItemStack playerItemStack,
			EnumHand hand) {
		ItemStack itemstack = this.getItemStackFromSlot(targetedSlot);
		if (player.capabilities.isCreativeMode && itemstack.isEmpty() && !playerItemStack.isEmpty()) {
			ItemStack itemstack2 = playerItemStack.copy();
			itemstack2.setCount(1);
			this.setItemStackToSlot(targetedSlot, itemstack2);
		} else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1) {
			if (itemstack.isEmpty()) {
				ItemStack itemstack1 = playerItemStack.copy();
				itemstack1.setCount(1);
				this.setItemStackToSlot(targetedSlot, itemstack1);
				playerItemStack.shrink(1);
			}
		} else {
			this.setItemStackToSlot(targetedSlot, playerItemStack);
			player.setHeldItem(hand, itemstack);
		}
	}

	public void setChild(boolean childMinion) {
		this.getDataManager().set(IS_CHILD, childMinion);
		this.setChildSize(childMinion);
	}

	@Override
	public boolean isChild() {
		return this.getDataManager().get(IS_CHILD);
	}

	public void setChildSize(boolean isChild) {
		float ratio = (isChild ? 0.5F : 1.0F);
		if (isInert())
			super.setSize(SIZE_Y * ratio, SIZE_X * ratio);
		else
			super.setSize(SIZE_X * ratio, SIZE_Y * ratio);
	}

	public void notifyDataManagerChange(DataParameter<?> key) {
		if (IS_CHILD.equals(key) || INERT.equals(key)) {
			this.setChildSize(this.isChild());
		}

		super.notifyDataManagerChange(key);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}
	
	/**
	 * Changes the state of this minion and updates accordingly
	 * @param corpse whether this minion is being resurrected or put down
	 */
	public void setCorpse(boolean corpse) {
		setInert(corpse);
		setDecompositionCountdown(getMaxTimeRemaining());
	}
	
	protected void setInert(boolean isCorpse) {
		this.getDataManager().set(INERT, isCorpse);

		if (isCorpse)
			this.setSize(SIZE_Y, SIZE_X);
		else
			this.setSize(SIZE_X, SIZE_Y);
	}

	/**
	 * @return Whether this minion is lying on the ground
	 */
	public boolean isInert() {
		return this.getDataManager().get(INERT);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("isInert", this.isInert());
		compound.setInteger("remainingTicks", getRemainingTicks());
		if (this.isChild())
			compound.setBoolean("isBaby", true);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setInert(compound.getBoolean("isInert"));
		this.setDecompositionCountdown(compound.getInteger("remainingTicks"));
		this.setChild(compound.getBoolean("isBaby"));
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
			ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
			if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
				if (itemstack.isItemStackDamageable()) {
					itemstack.setItemDamage(this.rand.nextInt(Math.min(itemstack.getMaxDamage() / 10, 50)));
				}

				this.entityDropItem(itemstack, 0.0F);
			}

		}
	}

	@Nullable
	@Override
    public UUID getOwnerId()
    {
        return this.getDataManager().get(OWNER_UNIQUE_ID).orNull();
    }

    public void setOwnerId(@Nullable UUID uuid)
    {
        this.getDataManager().set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
    }

    @Nullable
    @Override
    public EntityLivingBase getOwner()
    {
        UUID uuid = this.getOwnerId();
        return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
    }

}
