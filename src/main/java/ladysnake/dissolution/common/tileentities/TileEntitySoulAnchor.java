package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.common.blocks.BlockSoulAnchor;
import ladysnake.dissolution.common.blocks.BlockSoulAnchor.EnumPartType;
import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySoulAnchor extends TileEntity {
	
	protected BlockPos targetPos;
	protected BlockPos pipeExtremity;
	protected int targetDim;
	
	public TileEntitySoulAnchor() {
		super();
		this.targetPos = new BlockPos(0, 128, 0);
	}
	
	public TileEntitySoulAnchor(BlockPos pos, int dim) {
		super();
		this.targetPos = pos;
		this.targetDim = dim;
	}
	
	public BlockPos getExtremityPosition() {
		if(this.pipeExtremity != null) return pipeExtremity;
		
		pipeExtremity = this.pos;
		try {
			IBlockState thisState = world.getBlockState(this.pos);
			EnumPartType targetPart = (thisState.getValue(BlockSoulAnchor.PART) == EnumPartType.BASE) ? EnumPartType.CAP : EnumPartType.BASE;
			IBlockState targetState = ModBlocks.SOUL_ANCHOR.getDefaultState().withProperty(BlockSoulAnchor.PART, targetPart);
			pipeExtremity = (targetPart == EnumPartType.CAP) ? pipeExtremity.up() : pipeExtremity.down();
			while(world.getBlockState(pipeExtremity) != targetState){
				pipeExtremity = (targetPart == EnumPartType.CAP) ? pipeExtremity.up() : pipeExtremity.down();
				if(pipeExtremity.getY() > 255 || pipeExtremity.getY() < 0) {
					this.pipeExtremity = null;
					return this.pos;
				}
			}
			return pipeExtremity;
		} catch (NullPointerException e) {
			return this.pos;
		}
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
        return face == EnumFacing.UP || face == EnumFacing.DOWN;
    }
}
