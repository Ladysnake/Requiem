package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityWispInAJar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityWispInAJar();
    }

    //    @Nonnull
//    @Override
//    @Deprecated
//    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
//        return ModItems.SOUL_IN_A_JAR;
//    }
}
