package ladysnake.dissolution.common.tileentities;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ladysnake.dissolution.common.blocks.IPowerConductor;
import ladysnake.dissolution.common.blocks.IPowerConductor.IMachine;
import ladysnake.dissolution.common.init.ModBlocks;
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
		shouldRefresh = true;
	}
	
	public TileEntityPowerCore(boolean enabled) {
		this();
		this.enabled = enabled;
	}
	
	public void setEnabled(boolean b) {
		this.enabled = b;
		this.shouldRefresh = false;
		world.setBlockState(pos, world.getBlockState(pos).withProperty(IPowerConductor.ENABLED, this.isEnabled()));
		this.shouldRefresh = true;
		detectNetwork();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void updateNetwork() {
		if(enabled) {
			Set<BlockPos> old = new HashSet<>(nodes);
			detectNetwork();
			old.stream().filter(pos -> !nodes.contains(pos) && world.getBlockState(pos).getBlock() instanceof IPowerConductor).forEach(
					pos -> ((IPowerConductor)world.getBlockState(pos).getBlock()).setActivated(world, pos, false));
		}
	}
	
	public void detectNetwork() {
		nodes.clear();
		detectNetwork(this.pos, new LinkedList<>(), 0);
	}
	
	private void detectNetwork(BlockPos pos, List<BlockPos> searchedBlocks, int i) {
		if(++i > 100 || !(world.getBlockState(pos).getBlock() instanceof IPowerConductor) || searchedBlocks.contains(pos)) 
			return;
		
		searchedBlocks.add(pos);
		
		
		Block block = world.getBlockState(pos).getBlock();
		if(block instanceof IMachine) {
			nodes.add(pos);
			((IMachine)block).setActivated(world, pos, isEnabled());
		}
		
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
