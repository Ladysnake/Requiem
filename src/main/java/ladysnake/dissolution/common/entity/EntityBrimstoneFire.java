package ladysnake.dissolution.common.entity;

import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBrimstoneFire extends Entity {
	
	private DataParameter<Optional<BlockPos>> TARGET_FIRE = EntityDataManager.<Optional<BlockPos>>createKey(EntityBrimstoneFire.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private int ticksFired;
	public static final int MAX_FIRE_TIME = 10;

	public EntityBrimstoneFire(World worldIn) {
		super(worldIn);
        this.setSize(0.25F, 0.25F);
        this.ticksFired = -1;
	}
	
	public void fire() {
		this.ticksFired = 0;
	}
	
	/**
	 * Updates the brimstone attack
	 * @return true if the attack has ended
	 */
	public boolean updateFiring() {
		if(this.ticksFired >= 0) {
			System.out.println(this);
			if(this.ticksFired++ > MAX_FIRE_TIME) {
				this.ticksFired = -1;
				this.setDead();
				return true;
			}
		}
		return false;
	}
	
	public void setTarget(BlockPos pos) {
		this.getDataManager().set(TARGET_FIRE, Optional.of(pos));
		this.getDataManager().setDirty(TARGET_FIRE);
	}
	
	public BlockPos getTarget() {
		return this.getDataManager().get(TARGET_FIRE).get();
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(TARGET_FIRE, Optional.absent());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setTarget(new BlockPos(
				compound.getInteger("targetX"),
				compound.getInteger("targetY"),
				compound.getInteger("targetZ")));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		BlockPos bp = this.getTarget();
		compound.setInteger("targetX", bp.getX());
		compound.setInteger("targetY", bp.getY());
		compound.setInteger("targetZ", bp.getZ());
	}

	@Override
	public String toString() {
		return "EntityBrimstoneFire [TARGET_FIRE=" + TARGET_FIRE + ", ticksFired=" + ticksFired + "]";
	}
	
}
