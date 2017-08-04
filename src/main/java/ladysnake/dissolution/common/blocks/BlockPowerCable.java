package ladysnake.dissolution.common.blocks;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.tileentities.TileEntityPowerCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPowerCable extends Block implements IPowerConductor {

	public BlockPowerCable() {
		super(Material.CIRCUITS);
		this.setUnlocalizedName(Reference.Blocks.POWER_CABLE.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.POWER_CABLE.getRegistryName());
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		if(!worldIn.isRemote)
			findPowerCore(worldIn, pos).ifPresent(bp -> worldIn.scheduleUpdate(bp, worldIn.getBlockState(bp).getBlock(), 0));
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if(!worldIn.isRemote)
			findPowerCore(worldIn, pos).ifPresent(bp -> worldIn.scheduleUpdate(bp, worldIn.getBlockState(bp).getBlock(), 0));
	}
	
	public static Optional<BlockPos> findPowerCore(World world, BlockPos pos) {
		Optional res = scan(world, pos, new LinkedList<>(), 0);
		return res;
	}
	
	private static Optional<BlockPos> scan(World world, BlockPos pos, List<BlockPos> searchedBlocks, int i) {
		if(++i > 100 || !(world.getBlockState(pos).getBlock() instanceof IPowerConductor) || searchedBlocks.contains(pos)) 
			return Optional.empty();
		
		searchedBlocks.add(pos);
		
		if(world.getBlockState(pos).getBlock() instanceof BlockPowerCore)
			return Optional.of(pos);
		
		for(EnumFacing face : EnumFacing.values()) {
			Optional<BlockPos> result = scan(world, pos.offset(face), searchedBlocks, i);
			if(result.isPresent())
				return result;
		}
		
		return Optional.empty();
	}
	
	@Override
	public boolean isActivated(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos).getBlock() instanceof BlockPowerCable || IPowerConductor.super.isActivated(worldIn, pos);
	}

}
