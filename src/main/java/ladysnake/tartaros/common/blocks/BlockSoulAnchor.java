package ladysnake.tartaros.common.blocks;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class BlockSoulAnchor extends Block implements ITileEntityProvider {

	public BlockSoulAnchor() {
		super(Material.GLASS);
		this.setUnlocalizedName(Reference.Blocks.SOUL_ANCHOR.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.SOUL_ANCHOR.getRegistryName());
		this.setHardness(-1f);
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySoulAnchor();
	}

}
