package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.api.Soul;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class AbstractSoul extends Entity {

    protected Soul soul;
    protected int soulAge;
    protected double xTarget, yTarget, zTarget;

    public AbstractSoul(World worldIn) {
        this(worldIn, Soul.UNDEFINED);
    }

    public AbstractSoul(World worldIn, Soul soulIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.setEntityInvulnerable(true);
        this.setNoGravity(true);
        this.rotationYaw = (float) (Math.random() * 360.0D);
        this.motionX = (Math.random() * 0.2 - 0.1) * 2.0F;
        this.motionY = (Math.random() * 0.2) * 2.0F;
        this.motionZ = (Math.random() * 0.2 - 0.1) * 2.0F;
        this.soul = soulIn;
        this.isImmuneToFire = true;
    }

    public BlockPos getTargetPosition() {
        return new BlockPos(this.xTarget, this.yTarget + 0.5, this.zTarget);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ++this.soulAge;

//		if(this.world.isRemote) {
//			spawnParticles();
//		}
    }

    @SideOnly(Side.CLIENT)
    protected abstract void spawnParticles();

    public int getSoulAge() {
        return soulAge;
    }

    public Soul getSoul() {
        return soul;
    }

    @Override
    protected void entityInit() {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk
     * on. used for spiders and wolves to prevent them from trampling crops
     */
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public void setFire(int seconds) {
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
        try {
            this.soul = new Soul(compound.getCompoundTag("soul"));
        } catch (IllegalArgumentException e) {
            this.soul = Soul.UNDEFINED;
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("soul", soul.writeToNBT());
    }

}
