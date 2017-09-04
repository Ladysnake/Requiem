package ladysnake.dissolution.common.tileentities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockPowerCore;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityPowerCore extends TileEntity {
	
	private Set<BlockPos> nodes;
	private boolean enabled;
	private boolean shouldRefresh;
	
	public TileEntityPowerCore() {
		super();
		nodes = new HashSet<>();
		enabled = false;
		shouldRefresh = true;
	}
	
	public TileEntityPowerCore(boolean enabled) {
		this();
		this.enabled = enabled;
	}
	
	public void setEnabled(boolean b) {
		this.enabled = b;
		this.shouldRefresh = false;
		world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockPowerCore.ENABLED, this.isEnabled()));
		this.shouldRefresh = true;
		updateNetwork();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void updateNetwork() {
		Set<BlockPos> old = new HashSet<>(nodes);
		detectNetwork();
		old.stream().filter(pos -> !nodes.contains(pos) && world.getBlockState(pos).getBlock() instanceof IPowerConductor)
		.forEach(pos -> ((IPowerConductor)world.getBlockState(pos).getBlock()).setPowered(world, pos, false));
	}
	
	public void detectNetwork() {
		nodes.clear();
		detectNetwork(this.pos, new LinkedList<>(), 0);
	}
	
	private void detectNetwork(BlockPos pos, List<BlockPos> searchedBlocks, int i) {
		if(searchedBlocks.contains(pos))
			return;
		
		searchedBlocks.add(pos);
		if(++i > 100 || !(world.getBlockState(pos).getBlock() instanceof IPowerConductor)) 
			return;
		
		Block block = world.getBlockState(pos).getBlock();
		
		if(!((IPowerConductor)block).isConductive(world, pos))
			return;

		((IPowerConductor)block).setPowered(world, pos, isEnabled());
		nodes.add(pos);
		
		for(EnumFacing face : EnumFacing.values())
			detectNetwork(pos.offset(face), searchedBlocks, i);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return super.shouldRefresh(world, pos, oldState, newState) && shouldRefresh;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.enabled = (compound.getBoolean("enabled"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		compound.setBoolean("enabled", isEnabled());
		return compound;
	}

}
