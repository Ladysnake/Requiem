package ladysnake.dissolution.common.tileentities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		try {
			compound.setInteger("targX", targetPos.getX());
			compound.setInteger("targY", targetPos.getY());
			compound.setInteger("targZ", targetPos.getZ());
			compound.setInteger("targDim", targetDim);
		} catch (NullPointerException e) {}
		return compound;
	}

	@Override
	public String toString() {
		return "TileEntitySoulAnchor [targetPos=" + targetPos + ", targetDim=" + targetDim + "]";
	}
	
	@SideOnly(Side.CLIENT)
    public boolean shouldRenderFace(EnumFacing face)
    {
        return face == EnumFacing.UP;
    }
}
