package ladysnake.dissolution.common.blocks;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.handlers.CustomDissolutionTeleporter;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockSoulAnchor extends Block implements ITileEntityProvider, ISoulInteractable {
	
	public static final PropertyEnum<BlockSoulAnchor.EnumPartType> PART = 
			PropertyEnum.<BlockSoulAnchor.EnumPartType>create("part", BlockSoulAnchor.EnumPartType.class);

	public static List<Integer> scheduledDim = new ArrayList<Integer>();
	public static List<BlockPos> scheduledBP = new ArrayList<BlockPos>();
	
	public BlockSoulAnchor() {
		super(Material.GLASS);
		this.setUnlocalizedName(Reference.Blocks.SOUL_ANCHOR.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.SOUL_ANCHOR.getRegistryName());
		this.setDefaultState(this.blockState.getBaseState().withProperty(PART, EnumPartType.BASE));
		this.setHardness(4.0f);
		this.setLightLevel(1.0f);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if(state.getValue(PART) == BlockSoulAnchor.EnumPartType.CAP) return false;
		
		if (!(worldIn.getTileEntity(pos) instanceof TileEntitySoulAnchor) || !CapabilityIncorporealHandler.getHandler(playerIn).isIncorporeal()) return false;
		
		TileEntitySoulAnchor te = (TileEntitySoulAnchor) worldIn.getTileEntity(pos);
		
		if (!worldIn.isRemote) {
			try {
				CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP)playerIn, te.getTargetDim());
				playerIn.setPositionAndUpdate(te.getTargetPos().getX(), te.getTargetPos().getY(), te.getTargetPos().getZ());
				//worldIn.getBlockState(te.getTargetPos()).getBlock().onBlockActivated(
				//		worldIn, te.getTargetPos(), worldIn.getBlockState(te.getTargetPos()), playerIn, hand, facing, hitX, hitY, hitZ);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntitySoulAnchor ret;
		try {
			ret = new TileEntitySoulAnchor(scheduledBP.get(0), scheduledDim.get(0));
			scheduledBP.remove(0);
			scheduledDim.remove(0);
		} catch(IndexOutOfBoundsException e) {
			ret = new TileEntitySoulAnchor();
		}
		return ret;
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state); 
		if(state.getValue(PART) == EnumPartType.CAP) return;
		do {
			pos = pos.up();
		} while((worldIn.getBlockState(pos.up()) == Blocks.AIR.getDefaultState()) && pos.getY() < 250);
		worldIn.setBlockState(pos, getDefaultState().withProperty(PART, EnumPartType.CAP));
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		if(state.getValue(PART) == EnumPartType.CAP) {
			while((worldIn.getBlockState(pos) != ModBlocks.SOUL_ANCHOR.getDefaultState().withProperty(PART, EnumPartType.BASE)) && 
					pos.getY() > 0)
		    	pos = pos.down();
			if(pos.getY() > 0)
				worldIn.setBlockToAir(pos);
		} else if (state.getValue(PART) == EnumPartType.BASE) {
			while((worldIn.getBlockState(pos) != ModBlocks.SOUL_ANCHOR.getDefaultState().withProperty(PART, EnumPartType.CAP)) && 
					pos.getY() < 255)
		    	pos = pos.up();
			if(pos.getY() < 255)
				worldIn.setBlockToAir(pos);
		}
	}
	
	protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {PART});
    }
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PART,	(meta & 1) == 0 ? EnumPartType.BASE : EnumPartType.CAP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(PART) == EnumPartType.BASE ? 0 : 1);
	}
	
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {}
	
	public static enum EnumPartType implements IStringSerializable
    {
        CAP("cap"),
        BASE("base");

        private final String name;

        private EnumPartType(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }
    }

}
