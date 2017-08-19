package ladysnake.dissolution.common.blocks.powersystem;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AbstractPowerConductor extends Block implements IPowerConductor {

	public AbstractPowerConductor() {
		super(Material.CIRCUITS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, false));
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		if(!worldIn.isRemote && this.isPowered(worldIn, pos))
			updatePowerCore(worldIn, pos);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		updatePowerCore(worldIn, pos);
		worldIn.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
	}
	
	protected static void updatePowerCore(World world, BlockPos pos) {
		scan(world, pos, new LinkedList<>(), 0).ifPresent(bp -> world.scheduleUpdate(bp, world.getBlockState(bp).getBlock(), 0));
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
	public void setPowered(World worldIn, BlockPos pos, boolean b) {
		if(b != worldIn.getBlockState(pos).getValue(POWERED))
			worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(POWERED, b));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, POWERED);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(POWERED, (meta & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(POWERED) ? 1 : 0);
	}

}
