package ladysnake.dissolution.common.entity.minion;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIInert;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

public class EntityGenericMinion extends AbstractMinion implements IEntityAdditionalSpawnData {

    private EntityMob delegate;

    public EntityGenericMinion(World worldIn) {
        super(worldIn);
    }

    public EntityGenericMinion(World worldIn, EntityMob delegate) {
        super(worldIn, delegate.isChild());
        this.delegate = delegate;
        this.delegate.isDead = this.isDead;
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(delegate.getMaxHealth());
        this.setHealth(this.getMaxHealth());
        this.delegate.setHealth(this.getHealth());
        this.delegate.tasks.addTask(99, new EntityAIInert(true));
        this.equivalents.add(delegate.getClass());
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.delegate != null) {
            if (!this.world.isRemote)
                this.setHealth(delegate.getHealth());
            this.hurtTime = this.delegate.hurtTime;
            this.delegate.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.delegate.swingProgress = this.swingProgress;
            this.delegate.prevSwingProgress = this.prevSwingProgress;
            this.delegate.swingingHand = this.swingingHand;
            this.delegate.swingProgressInt = this.swingProgressInt;
            this.delegate.limbSwingAmount = this.limbSwingAmount;
            this.delegate.prevLimbSwingAmount = this.prevLimbSwingAmount;
            this.delegate.limbSwing = this.limbSwing;
            this.delegate.isSwingInProgress = this.isSwingInProgress;
            this.delegate.motionX = this.motionX;
            this.delegate.motionY = this.motionY;
            this.delegate.motionZ = this.motionZ;
            this.delegate.renderYawOffset = this.renderYawOffset;
            this.delegate.prevRenderYawOffset = this.prevRenderYawOffset;
            this.delegate.prevRotationYawHead = this.prevRotationYawHead;
            this.delegate.rotationYawHead = this.rotationYawHead;
            this.delegate.onUpdate();
        }
    }

    @Override
    protected void handleSunExposure() {
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMinionAttack(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.applyEntityAI();
    }

    public EntityMob getDelegate() {
        return delegate;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return delegate == null ? super.getBlockPathWeight(pos) : delegate.getBlockPathWeight(pos);
    }

    @Nonnull
    @Override
    public BlockPos getHomePosition() {
        return delegate == null ? super.getHomePosition() : delegate.getHomePosition();
    }

    @Override
    public float getMaximumHomeDistance() {
        return delegate == null ? super.getMaximumHomeDistance() : delegate.getMaximumHomeDistance();
    }

    @Override
    public float getPathPriority(@Nonnull PathNodeType nodeType) {
        return delegate == null ? super.getPathPriority(nodeType) : delegate.getPathPriority(nodeType);
    }

    @Nonnull
    @Override
    public EntityLookHelper getLookHelper() {
        return delegate == null ? super.getLookHelper() : delegate.getLookHelper();
    }

    @Nonnull
    @Override
    public EntityMoveHelper getMoveHelper() {
        return delegate == null ? super.getMoveHelper() : delegate.getMoveHelper();
    }

    @Nonnull
    @Override
    public EntityJumpHelper getJumpHelper() {
        return delegate == null ? super.getJumpHelper() : delegate.getJumpHelper();
    }

    @Nonnull
    @Override
    public PathNavigate getNavigator() {
        return delegate == null ? super.getNavigator() : delegate.getNavigator();
    }

    @Nonnull
    @Override
    public EntitySenses getEntitySenses() {
        return delegate == null ? super.getEntitySenses() : delegate.getEntitySenses();
    }

    @Override
    public boolean isBurning() {
        return delegate == null ? super.isBurning() : delegate.isBurning();
    }

    @Override
    public int getVerticalFaceSpeed() {
        return delegate == null ? super.getVerticalFaceSpeed() : delegate.getVerticalFaceSpeed();
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return delegate == null ? super.getHorizontalFaceSpeed() : delegate.getHorizontalFaceSpeed();
    }

    @Override
    public float getRenderSizeModifier() {
        return delegate == null ? super.getRenderSizeModifier() : delegate.getRenderSizeModifier();
    }

    @Override
    public int getMaxFallHeight() {
        return delegate == null ? super.getMaxFallHeight() : delegate.getMaxFallHeight();
    }

    @Override
    public float getEyeHeight() {
        return delegate == null ? super.getEyeHeight() : delegate.getEyeHeight();
    }

    @Override
    public void addPotionEffect(PotionEffect potioneffectIn) {
        if (delegate == null)
            super.addPotionEffect(potioneffectIn);
        else
            delegate.addPotionEffect(potioneffectIn);
    }

    @Override
    public boolean addTag(String tag) {
        return delegate == null ? super.addTag(tag) : delegate.addTag(tag);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = delegate == null ? super.attackEntityAsMob(entityIn) : delegate.attackEntityAsMob(entityIn);
        if (entityIn instanceof EntityLivingBase)
            ((EntityLivingBase) entityIn).setRevengeTarget(this);
        return flag;
    }

    @Override
    public void attackEntityWithRangedAttack(@Nonnull EntityLivingBase target, float distanceFactor) {
        if (delegate instanceof IRangedAttackMob)
            ((IRangedAttackMob) delegate).attackEntityWithRangedAttack(target, distanceFactor);
        else
            super.attackEntityWithRangedAttack(target, distanceFactor);
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        super.addVelocity(x, y, z);
        this.delegate.addVelocity(x, y, z);
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        if (this.delegate == null)
            super.setItemStackToSlot(slotIn, stack);
        else
            delegate.setItemStackToSlot(slotIn, stack);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean ret = false;
        Entity entity = source.getTrueSource();
        //if(!(this.isBeingRidden() && entity != null && this.isRidingOrBeingRiddenBy(entity))) {
        if (delegate == null)
            ret = super.attackEntityFrom(source, amount);
        else if (!delegate.equals(entity)) {
            ret = delegate.attackEntityFrom(source, amount);
            this.isAirBorne = delegate.isAirBorne;
            this.motionX = delegate.motionX;
            this.motionY = delegate.motionY;
            this.motionZ = delegate.motionZ;
        }
        //}
        return ret;
    }

    @Override
    public void clearActivePotions() {
        if (delegate == null)
            super.clearActivePotions();
        else
            delegate.clearActivePotions();
    }

    @Override
    public PotionEffect getActivePotionEffect(Potion potionIn) {
        return delegate == null ? super.getActivePotionEffect(potionIn) : delegate.getActivePotionEffect(potionIn);
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return delegate == null ? super.getActivePotionEffects() : delegate.getActivePotionEffects();
    }

    @Override
    public boolean isPotionActive(Potion potionIn) {
        return super.isPotionActive(potionIn);
    }

    @Override
    public void awardKillScore(Entity p_191956_1_, int p_191956_2_, DamageSource p_191956_3_) {
        if (delegate == null)
            super.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
        else
            delegate.awardKillScore(p_191956_1_, p_191956_2_, p_191956_3_);
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return delegate == null ? super.canBeAttackedWithItem() : delegate.canBeAttackedWithItem();
    }

    @Override
    public void setActiveHand(EnumHand hand) {
        if (delegate == null)
            super.setActiveHand(hand);
        else
            delegate.setActiveHand(hand);
    }

    @Override
    public boolean isHandActive() {
        return this.delegate == null ? super.isHandActive() : delegate.isHandActive();
    }

    @Nonnull
    @Override
    public EnumHand getActiveHand() {
        return this.delegate == null ? super.getActiveHand() : delegate.getActiveHand();
    }

    @Nonnull
    @Override
    public ItemStack getActiveItemStack() {
        return this.delegate == null ? super.getActiveItemStack() : delegate.getActiveItemStack();
    }

    @Override
    public void stopActiveHand() {
        if (this.getActiveItemStack().getItem() instanceof ItemBow) {
            this.fireBow();
        } else {
            if (this.delegate == null)
                super.stopActiveHand();
            else
                this.delegate.stopActiveHand();
        }
    }

    @Override
    public float getAIMoveSpeed() {
        return/* delegate == null ?*/ super.getAIMoveSpeed();// : delegate.getAIMoveSpeed();
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return delegate == null ? super.doesEntityNotTriggerPressurePlate() : delegate.doesEntityNotTriggerPressurePlate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (delegate != null && delegate.hasCapability(capability, facing))
            return delegate.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        return entityIn.equals(delegate) || super.isOnSameTeam(entityIn);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        if (delegate instanceof IRangedAttackMob)
            ((IRangedAttackMob) delegate).setSwingingArms(swingingArms);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return delegate == null ? super.getAmbientSound() : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return delegate == null ? super.getDeathSound() : null;
    }

    @Override
    protected SoundEvent getFallSound(int heightIn) {
        return delegate == null ? super.getFallSound(heightIn) : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return delegate == null ? super.getHurtSound(damageSourceIn) : null;
    }

    @Override
    public void playLivingSound() {
        if (delegate == null)
            super.playLivingSound();
    }

    @Override
    protected SoundEvent getSwimSound() {
        return delegate == null ? super.getSwimSound() : null;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return delegate == null ? super.getSplashSound() : null;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        if (delegate == null)
            super.playStepSound(pos, blockIn);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        Entity ent = EntityList.createEntityFromNBT(compound.getCompoundTag("delegate"), this.world);
        if (ent instanceof EntityMob)
            this.delegate = (EntityMob) ent;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound ret = super.writeToNBT(compound);
        if (delegate != null)
            ret.setTag("delegate", delegate.serializeNBT());
        return ret;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer wrapper = new PacketBuffer(buffer);
        if (delegate != null)
            wrapper.writeCompoundTag(this.delegate.serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        try {
            NBTTagCompound delegateCompound = new PacketBuffer(additionalData).readCompoundTag();
            if (delegateCompound != null) {
                Entity ent = EntityList.createEntityFromNBT(delegateCompound, this.world);
                if (ent instanceof EntityMob)
                    this.delegate = (EntityMob) ent;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
