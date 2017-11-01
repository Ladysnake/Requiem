package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockCrucible extends BlockGenericContainer {
    public BlockCrucible() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))
            return true;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityCrucible) {
            playerIn.setHeldItem(hand, ((TileEntityCrucible) tile).insertItem(playerIn.getHeldItem(hand)));
        }
        return true;
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
