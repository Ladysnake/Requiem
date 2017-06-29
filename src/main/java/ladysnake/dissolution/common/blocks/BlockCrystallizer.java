package ladysnake.dissolution.common.blocks;

import java.util.Random;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.inventory.GuiCrystallizer;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.tileentities.TileEntityCrystallizer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCrystallizer extends BlockContainer implements ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool LIT = PropertyBool.create("lit");

	public BlockCrystallizer() {
		super(Material.ROCK);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setUnlocalizedName(Reference.Blocks.CRYSTALLIZER.getUnlocalizedName());
		setRegistryName(Reference.Blocks.CRYSTALLIZER.getRegistryName());
		TileEntityCrystallizer.init();
		this.setHardness(2.0f);
		this.setHarvestLevel("pickaxe", 1);
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
				new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2)).withProperty(LIT,
				(meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2 + (state.getValue(LIT) ? 8 : 0);
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, LIT);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCrystallizer();
	}

	public static TileEntityCrystallizer getTE(IBlockAccess world, BlockPos pos) {
		return (TileEntityCrystallizer) world.getTileEntity(pos);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {

		return state.withProperty(LIT, getTE(world, pos).isBurning());
	}
	

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityCrystallizer) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityCrystallizer) tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return true;

		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityCrystallizer) {
			playerIn.openGui(Dissolution.instance, GuiProxy.CRYSTALLIZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}
	
    public static void setState(boolean active, World worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        TileEntityCrystallizer te = (TileEntityCrystallizer) worldIn.getTileEntity(pos);
        te.keepInventory = true;

        if (active)
        {
            worldIn.setBlockState(pos, ModBlocks.CRYSTALLIZER.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, true), 3);
            worldIn.setBlockState(pos, ModBlocks.CRYSTALLIZER.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, true), 3);
        }
        else
        {
            worldIn.setBlockState(pos, ModBlocks.CRYSTALLIZER.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, false), 3);
            worldIn.setBlockState(pos, ModBlocks.CRYSTALLIZER.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, false), 3);
        }

        te.keepInventory = false;
/*
        if (te != null)
        {
            te.validate();
            worldIn.setTileEntity(pos, te);
        }*/
    }
}
