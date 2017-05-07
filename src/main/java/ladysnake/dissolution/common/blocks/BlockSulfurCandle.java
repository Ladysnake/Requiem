package ladysnake.dissolution.common.blocks;

import java.util.List;

import javax.annotation.Nullable;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.tileentities.TileEntityMercuryCandle;
import ladysnake.dissolution.common.tileentities.TileEntitySulfurCandle;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSulfurCandle extends Block implements ITileEntityProvider {
	
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625 * 4, 0, 0.0625 * 4, 0.0625 * 12, 0.0625 * 16, 0.0625 * 12);
	private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.300D, 0.0D, 0.300D, 0.700D, 1.0D, 0.700D);

	public BlockSulfurCandle() {
		super(Material.GLASS);
		this.setUnlocalizedName(Reference.Blocks.SULFUR_CANDLE.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.SULFUR_CANDLE.getRegistryName());
		this.setHardness(1.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(playerIn);
		playerCorp.setSoulCandleNearby(true, 2);
		/*
		if(worldIn.isRemote) {
			if(Minecraft.getMinecraft().entityRenderer.isShaderActive())
				Minecraft.getMinecraft().entityRenderer.stopUseShader();
			else
				Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/deconverge.json"));
		}
		*/
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySulfurCandle();
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX);
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	

}
