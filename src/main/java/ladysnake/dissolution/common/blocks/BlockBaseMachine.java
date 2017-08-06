package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.IPowerConductor.IMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBaseMachine extends Block implements IMachine {
	
	public BlockBaseMachine() {
		super(Material.CIRCUITS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ENABLED, false));
	}

	@Override
	public void setActivated(World worldIn, BlockPos pos, boolean b) {
		worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(ENABLED, b));
	}	
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if(!worldIn.isRemote)
			BlockPowerCable.findPowerCore(worldIn, pos).ifPresent(bp -> worldIn.scheduleUpdate(bp, worldIn.getBlockState(bp).getBlock(), 0));
	}

	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ENABLED, (meta & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ENABLED) ? 1 : 0;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ENABLED);
	}

}
