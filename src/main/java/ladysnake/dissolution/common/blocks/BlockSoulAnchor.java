package ladysnake.dissolution.common.blocks;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.handlers.CustomTartarosTeleporter;
import ladysnake.dissolution.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSoulAnchor extends Block implements ITileEntityProvider {
	
	public static List<Integer> scheduledDim = new ArrayList<Integer>();
	public static List<BlockPos> scheduledBP = new ArrayList<BlockPos>();
	
	public BlockSoulAnchor() {
		super(Material.GLASS);
		this.setUnlocalizedName(Reference.Blocks.SOUL_ANCHOR.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.SOUL_ANCHOR.getRegistryName());
		this.setHardness(-1f);
		this.setLightLevel(15);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!(worldIn.getTileEntity(pos) instanceof TileEntitySoulAnchor) || 
				!IncorporealDataHandler.getHandler(playerIn).isIncorporeal()) return false;
		
		TileEntitySoulAnchor te = (TileEntitySoulAnchor) worldIn.getTileEntity(pos);
		
		if (!worldIn.isRemote) {
			try {
				CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP)playerIn, te.getTargetDim());
				playerIn.setPositionAndUpdate(te.getTargetPos().getX(), te.getTargetPos().getY(), te.getTargetPos().getZ());
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

}
