package ladysnake.dissolution.common.blocks.alchemysystem;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBarrage extends AbstractPowerConductor {

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    public BlockBarrage() {
        super();
        this.setDefaultState(super.getDefaultState().withProperty(ENABLED, true));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.updateState(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        updateState(worldIn, pos, state);
    }

    private void updateState(World worldIn, BlockPos pos, IBlockState state) {
        boolean flag = !worldIn.isBlockPowered(pos);

        if (flag != state.getValue(ENABLED)) {
            worldIn.setBlockState(pos, state.withProperty(ENABLED, flag));
            if (!worldIn.isRemote)
                updatePowerCore(worldIn, pos);
        }
    }

    @Override
    public boolean isConductive(IBlockAccess worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getValue(ENABLED);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWERED, ENABLED);
    }

}
