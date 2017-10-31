package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockCrucible extends BlockPrimitiveContainer {
    public BlockCrucible() {
        super(Material.ROCK);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCrucible();
    }
}
