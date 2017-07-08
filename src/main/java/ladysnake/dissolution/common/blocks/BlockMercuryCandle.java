package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.tileentities.TileEntitySoulCandle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMercuryCandle extends Block implements ITileEntityProvider {
	
	public BlockMercuryCandle() {
		super(Material.GLASS);
		this.setUnlocalizedName(Reference.Blocks.MERCURY_CANDLE.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.MERCURY_CANDLE.getRegistryName());
		this.setHardness(1.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(playerIn);
		playerCorp.setSoulCandleNearby(true, 1);
		return true;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return super.createBlockState();
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySoulCandle();
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

}
