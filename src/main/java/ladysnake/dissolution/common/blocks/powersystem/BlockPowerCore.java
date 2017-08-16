package ladysnake.dissolution.common.blocks;

import java.util.Random;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.tileentities.TileEntityPowerCore;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPowerCore extends Block implements ITileEntityProvider, IPowerConductor {
	
	public BlockPowerCore() {
		super(Material.CIRCUITS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ENABLED, false));
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if(worldIn.getTileEntity(pos) instanceof TileEntityPowerCore)
			((TileEntityPowerCore)worldIn.getTileEntity(pos)).updateNetwork();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote)
			setActivated(worldIn, pos, !isActivated(worldIn, pos));
		return true;
	}
	
	@Override
	public void setActivated(World worldIn, BlockPos pos, boolean b) {
		if(worldIn.getTileEntity(pos) instanceof TileEntityPowerCore) {
			TileEntityPowerCore tepc = ((TileEntityPowerCore)worldIn.getTileEntity(pos));
			tepc.setEnabled(!tepc.isEnabled());
		}
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityPowerCore(getStateFromMeta(meta).getValue(ENABLED));
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		worldIn.scheduleUpdate(pos, this, 0);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ENABLED);
	}

}
