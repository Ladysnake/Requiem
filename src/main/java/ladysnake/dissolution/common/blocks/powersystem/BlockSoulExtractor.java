package ladysnake.dissolution.common.blocks;

import java.util.List;

import javax.annotation.Nullable;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityCrystallizer;
import ladysnake.dissolution.common.tileentities.TileEntitySoulExtractor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSoulExtractor extends Block implements ITileEntityProvider {
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool LIT = PropertyBool.create("lit");
	public static final PropertyBool OUTPUT = PropertyBool.create("output");
	
	public BlockSoulExtractor() {
		super(Material.ROCK);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(1.0f);
		this.setHarvestLevel("pickaxe", 1);
	}
	
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
	
	@Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2)).withProperty(LIT, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex()-2 + (state.getValue(LIT) ? 8 : 0);
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
		return new TileEntitySoulExtractor();
	}
	
	private TileEntitySoulExtractor getTE(IBlockAccess world, BlockPos pos) {
		if (world.getTileEntity(pos) instanceof TileEntitySoulExtractor)
			return (TileEntitySoulExtractor) world.getTileEntity(pos);
		return null;
	}
	
	@Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		//System.out.println(!getTE(world, pos).isEmpty());
        return state.withProperty(LIT, !getTE(world, pos).isEmpty());
    }
		
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(getTE(worldIn, pos) != null) {
			if(playerIn.getHeldItem(hand).getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND)) {
				playerIn.getHeldItem(hand).setCount(getTE(worldIn, pos).addSoulSand(playerIn.getHeldItem(hand).getCount()));
			}
			else if(playerIn.getHeldItem(hand).getItem() == Items.GLASS_BOTTLE) {
				if(getTE(worldIn, pos).retrieveSoul()){
					playerIn.getHeldItem(hand).shrink(1);
					playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.SOUL_IN_A_BOTTLE));
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		getTE(worldIn, pos).emptyInWorld(worldIn);
	}
	
	public static void setState(boolean active, World worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        TileEntitySoulExtractor te = (TileEntitySoulExtractor) worldIn.getTileEntity(pos);
        te.keepInventory = true;

        if (active)
        {
            worldIn.setBlockState(pos, ModBlocks.SOUL_EXTRACTOR.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, true), 3);
            worldIn.setBlockState(pos, ModBlocks.SOUL_EXTRACTOR.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, true), 3);
        }
        else
        {
            worldIn.setBlockState(pos, ModBlocks.SOUL_EXTRACTOR.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, false), 3);
            worldIn.setBlockState(pos, ModBlocks.SOUL_EXTRACTOR.getDefaultState().withProperty(FACING, iblockstate.getValue(FACING)).withProperty(LIT, false), 3);
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
