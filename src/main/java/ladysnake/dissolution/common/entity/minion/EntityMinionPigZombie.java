package ladysnake.dissolution.common.entity.minion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityMinionPigZombie extends EntityMinionZombie {
	private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
	private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = (new AttributeModifier(ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.06D, 0)).setSaved(false);

	private int angerLevel;
	private int randomSoundDelay;
	private UUID angerTargetUUID;

	public EntityMinionPigZombie(World worldIn) {
		this(worldIn, false);
	}

	public EntityMinionPigZombie(World worldIn, boolean isChild) {
		super(worldIn, false, isChild);
	}

	protected void updateAITasks()
	{
		IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

		if (this.isAngry()) {
			if (!this.isChild() && !iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
				iattributeinstance.applyModifier(ATTACK_SPEED_BOOST_MODIFIER);
			}

			--this.angerLevel;
		}
		else if (iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
			iattributeinstance.removeModifier(ATTACK_SPEED_BOOST_MODIFIER);
		}

		if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
			this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_ANGRY, this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
		}

		if (this.angerLevel > 0 && this.angerTargetUUID != null && this.getRevengeTarget() == null) {
			EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
			this.setRevengeTarget(entityplayer);
			this.attackingPlayer = entityplayer;
			this.recentlyHit = this.getRevengeTimer();
		}

		super.updateAITasks();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) return false;
		if(source.getTrueSource() instanceof EntityLivingBase) {
			becomeAngryAt(source.getTrueSource());
		}
		return super.attackEntityFrom(source, amount);
	}

	private void becomeAngryAt(Entity bully) {
		this.angerLevel = 400 + this.rand.nextInt(400);
		this.randomSoundDelay = this.rand.nextInt(40);

		if (bully instanceof EntityLivingBase) {
			this.setRevengeTarget((EntityLivingBase)bully);
		}
	}

	public void setRevengeTarget(@Nullable EntityLivingBase livingBase) {
		super.setRevengeTarget(livingBase);
		if (livingBase != null) {
			this.angerTargetUUID = livingBase.getUniqueID();
		}
	}

	@Override
	protected void handleSunExposure() {}
	
	@Override
	protected SoundEvent getAmbientSound()
    {
        return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_DEATH;
    }

    protected SoundEvent getStepSound()
    {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_STEP;
    }

    public boolean isAngry()
	{
		return this.angerLevel > 0;
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setShort("Anger", (short)this.angerLevel);

		if (this.angerTargetUUID != null) {
			compound.setString("HurtBy", this.angerTargetUUID.toString());
		}
		else {
			compound.setString("HurtBy", "");
		}
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.angerLevel = compound.getShort("Anger");
		String s = compound.getString("HurtBy");

		if (!s.isEmpty()) {
			this.angerTargetUUID = UUID.fromString(s);
			EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
			this.setRevengeTarget(entityplayer);

			if (entityplayer != null) {
				this.attackingPlayer = entityplayer;
				this.recentlyHit = this.getRevengeTimer();
			}
		}
	}

}
