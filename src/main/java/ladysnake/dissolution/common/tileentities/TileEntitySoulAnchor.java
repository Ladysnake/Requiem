package ladysnake.dissolution.common.tileentities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntitySoulAnchor extends TileEntity {
	
	protected BlockPos targetPos;
	protected int targetDim;
	
	public TileEntitySoulAnchor() {
		super();
	}
	
	public TileEntitySoulAnchor(BlockPos pos, int dim) {
		super();
		this.targetPos = pos;
		this.targetDim = dim;
	}
	
	public void setTargetPos(BlockPos targetPos) {
		this.targetPos = targetPos;
	}
	
	public BlockPos getTargetPos() {
		return targetPos;
	}
	
	public void setTargetDim(int targetDim) {
		this.targetDim = targetDim;
	}
	
	public int getTargetDim() {
		return targetDim;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		targetPos = new BlockPos(compound.getInteger("targX"), compound.getInteger("targY"), compound.getInteger("targZ"));
		targetDim = compound.getInteger("targDim");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("targX", targetPos.getX());
		compound.setInteger("targY", targetPos.getY());
		compound.setInteger("targZ", targetPos.getZ());
		compound.setInteger("targDim", targetDim);
		return compound;
	}

	@Override
	public String toString() {
		return "TileEntitySoulAnchor [targetPos=" + targetPos + ", targetDim=" + targetDim + "]";
	}
}
