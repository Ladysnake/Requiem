package ladysnake.dissolution.common.blocks.alchemysystem;

import ladysnake.dissolution.client.models.blocks.PropertyBoolean;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.tileentities.TileEntityDistillatePipe;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockDistillatePipe extends Block {

    public static final PropertyBoolean NORTH = new PropertyBoolean("north");
    public static final PropertyBoolean SOUTH = new PropertyBoolean("south");
    public static final PropertyBoolean WEST = new PropertyBoolean("west");
    public static final PropertyBoolean EAST = new PropertyBoolean("east");
    public static final PropertyBoolean UP = new PropertyBoolean("up");
    public static final PropertyBoolean DOWN = new PropertyBoolean("down");

    public BlockDistillatePipe() {
        super(Material.CIRCUITS);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[]{NORTH, SOUTH, WEST, EAST, UP, DOWN};
        return new ExtendedBlockState(this, new IProperty[0], unlistedProperties);
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;

        boolean north = shouldAttach(world, EnumFacing.NORTH, pos);
        boolean south = shouldAttach(world, EnumFacing.SOUTH, pos);
        boolean west = shouldAttach(world, EnumFacing.WEST, pos);
        boolean east = shouldAttach(world, EnumFacing.EAST, pos);
        boolean up = shouldAttach(world, EnumFacing.UP, pos);
        boolean down = shouldAttach(world, EnumFacing.DOWN, pos);

        return extendedBlockState
                .withProperty(NORTH, north)
                .withProperty(SOUTH, south)
                .withProperty(WEST, west)
                .withProperty(EAST, east)
                .withProperty(UP, up)
                .withProperty(DOWN, down);
    }

    private boolean shouldAttach(IBlockAccess world, EnumFacing facing, BlockPos pipePos) {
        BlockPos pos = pipePos.offset(facing);
        TileEntity te = world.getTileEntity(pos);
        return te != null && te.hasCapability(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, facing.getOpposite());
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        double x1 = 0.375;
        double y1 = 0.375;
        double z1 = 0.375;
        double x2 = 0.625;
        double y2 = 0.625;
        double z2 = 0.625;
        if (shouldAttach(source, EnumFacing.DOWN, pos))
            y1 = 0;
        if (shouldAttach(source, EnumFacing.UP, pos))
            y2 = 1;
        if (shouldAttach(source, EnumFacing.NORTH, pos))
            z1 = 0;
        if (shouldAttach(source, EnumFacing.SOUTH, pos))
            z2 = 1;
        if (shouldAttach(source, EnumFacing.WEST, pos))
            x1 = 0;
        if (shouldAttach(source, EnumFacing.EAST, pos))
            x2 = 1;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityDistillatePipe();
    }

}
