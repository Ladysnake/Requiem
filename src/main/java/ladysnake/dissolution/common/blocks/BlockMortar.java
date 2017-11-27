package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMortar extends BlockGenericContainer {
    public BlockMortar() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))
            return true;
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityMortar) {
            if (OreDictHelper.doesItemMatch(playerIn.getHeldItem(hand), OreDictHelper.PESTLE, OreDictHelper.PESTLE_AND_MORTAR))
                ((TileEntityMortar) tile).crush();
        }
        return true;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.2, 0, 0.2, 0.8, 0.3, 0.8);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityMortar();
    }
}
