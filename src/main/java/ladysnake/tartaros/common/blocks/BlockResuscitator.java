package ladysnake.tartaros.common.blocks;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.tileentities.TileEntityCrystallizer;
import ladysnake.tartaros.common.tileentities.TileEntityResuscitator;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockResuscitator extends Block implements ITileEntityProvider {

	public BlockResuscitator() {
		super(Material.IRON);
		setUnlocalizedName(Reference.Blocks.RESUSCITATOR.getUnlocalizedName());
		setRegistryName(Reference.Blocks.RESUSCITATOR.getRegistryName());
		TileEntityCrystallizer.init();
		this.setHardness(1.0f);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityResuscitator) {
			TileEntityResuscitator resuscitator = (TileEntityResuscitator) te;

			if (!playerIn.isSneaking() && playerIn.getHeldItem(hand).isEmpty() == false) {
				resuscitator.AddItem(playerIn.getHeldItem(hand));
			} else if(!playerIn.getHeldItem(hand).isEmpty()){
				resuscitator.RemoveItem(playerIn.getHeldItem(hand));
			}
			System.out.println("items : " + resuscitator.itemName);
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	public static TileEntityResuscitator getTE(IBlockAccess world, BlockPos pos) {

		return (TileEntityResuscitator) world.getTileEntity(pos);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {

		return new TileEntityResuscitator();
	}

}
