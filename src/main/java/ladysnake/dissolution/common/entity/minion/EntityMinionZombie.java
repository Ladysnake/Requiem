package ladysnake.dissolution.common.entity.minion;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;

public class EntityMinionZombie extends AbstractMinion implements IEntityAdditionalSpawnData {

    private boolean isHusk;

    /**
     * Used by minecraft to spawn the entity internally
     */
    public EntityMinionZombie(World worldIn) {
        this(worldIn, false, false);
    }

    /**
     * Creates a zombie minion
     *
     * @param isHusk false for the default zombie
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

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        if (flag) {
            float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();

            if (this.getHeldItemMainhand().isEmpty()) {
                if (this.isBurning() && this.rand.nextFloat() < f * 0.3F) {
                    entityIn.setFire(2 * (int) f);
                }
                if (isHusk() && entityIn instanceof EntityLivingBase) {
                    ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int) f));
                }
            }
        }
        return flag;
    }


    @Override
    protected void handleSunExposure() {
        if (!this.isHusk)
            super.handleSunExposure();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return (isInert()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return (isInert()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return (isInert()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return (isInert()) ? null : (this.isHusk()) ? SoundEvents.ENTITY_HUSK_AMBIENT : SoundEvents.ENTITY_ZOMBIE_STEP;
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    @Nonnull
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
        buffer.writeBoolean(isHusk());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        setHusk(additionalData.readBoolean());
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
    }

}
