package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockWisp extends Block {
    public BlockWisp() {
        super(Material.GLASS);
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.SOUL_IN_A_FLASK;
    }
}
