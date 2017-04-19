package ladysnake.tartaros.common.blocks;

import ladysnake.tartaros.common.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;

public class BlockEctoplasm extends Block {

	public BlockEctoplasm() {
		super(Material.SNOW);

    	this.setUnlocalizedName(Reference.Blocks.ECTOPLASM.getUnlocalizedName());
    	this.setRegistryName(Reference.Blocks.ECTOPLASM.getRegistryName());
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
}
