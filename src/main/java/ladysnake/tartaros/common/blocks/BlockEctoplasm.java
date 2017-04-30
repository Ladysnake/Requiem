package ladysnake.tartaros.common.blocks;

import ladysnake.tartaros.common.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEctoplasm extends Block {

	public BlockEctoplasm() {
		super(Material.ICE);

    	this.setUnlocalizedName(Reference.Blocks.ECTOPLASM.getUnlocalizedName());
    	this.setRegistryName(Reference.Blocks.ECTOPLASM.getRegistryName());
    	this.setHardness(0.5f);
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote && !player.isCreative())
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
        }
	}
	
	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
            entityIn.fall(fallDistance, 0.0F);
    }
	
	
}
