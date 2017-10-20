package ladysnake.dissolution.common.entity.minion;

import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import java.io.IOException;

public class EntityGenericMinion extends AbstractMinion implements IEntityAdditionalSpawnData{

    private EntityMob delegate;

    public EntityGenericMinion(World worldIn) {
        super(worldIn);
    }

    public EntityGenericMinion(World worldIn, EntityMob delegate) {
        super(worldIn, delegate.isChild());
        this.delegate = delegate;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
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
        return delegate.getBlockPathWeight(pos);
    }

    @Nonnull
    @Override
    public BlockPos getHomePosition() {
        return delegate.getHomePosition();
    }

    @Override
    public float getMaximumHomeDistance() {
        return delegate.getMaximumHomeDistance();
    }

    @Override
    public float getPathPriority(@Nonnull PathNodeType nodeType) {
        return delegate.getPathPriority(nodeType);
    }

    @Nonnull
    @Override
    public EntityLookHelper getLookHelper() {
        return delegate.getLookHelper();
    }

    @Nonnull
    @Override
    public EntityMoveHelper getMoveHelper() {
        return delegate.getMoveHelper();
    }

    @Nonnull
    @Override
    public EntityJumpHelper getJumpHelper() {
        return delegate.getJumpHelper();
    }

    @Nonnull
    @Override
    public PathNavigate getNavigator() {
        return delegate.getNavigator();
    }

    @Nonnull
    @Override
    public EntitySenses getEntitySenses() {
        return delegate.getEntitySenses();
    }

    @Override
    public void playLivingSound() {
        delegate.playLivingSound();
    }

    @Override
    public int getVerticalFaceSpeed() {
        return delegate.getVerticalFaceSpeed();
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return delegate.getHorizontalFaceSpeed();
    }

    @Override
    public float getRenderSizeModifier() {
        return delegate.getRenderSizeModifier();
    }

    @Override
    public int getMaxFallHeight() {
        return delegate.getMaxFallHeight();
    }

    @Override
    public float getEyeHeight() {
        return delegate.getEyeHeight();
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        if(delegate instanceof IRangedAttackMob)
            ((IRangedAttackMob) delegate).setSwingingArms(swingingArms);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        Entity ent = EntityList.createEntityFromNBT(compound.getCompoundTag("delegate"), this.world);
        if(ent instanceof EntityMob)
            this.delegate = (EntityMob) ent;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound ret = super.writeToNBT(compound);
        ret.setTag("delegate", delegate.writeToNBT(new NBTTagCompound()));
        return ret;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer wrapper = new PacketBuffer(buffer);
        wrapper.writeCompoundTag(this.delegate.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        try {
            NBTTagCompound delegateCompound = new PacketBuffer(additionalData).readCompoundTag();
            if(delegateCompound != null) {
                Entity ent = EntityList.createEntityFromNBT(delegateCompound, this.world);
                if (ent instanceof EntityMob)
                    this.delegate = (EntityMob) ent;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
