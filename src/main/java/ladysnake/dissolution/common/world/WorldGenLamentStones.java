package ladysnake.dissolution.common.world;

import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

public class WorldGenLamentStones extends WorldGenerator {
    @Override
    public boolean generate(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos position) {
        for (IBlockState iblockstate = worldIn.getBlockState(position);
             (iblockstate.getBlock().isAir(iblockstate, worldIn, position)
                     || iblockstate.getBlock().isLeaves(iblockstate, worldIn, position))
                     && position.getY() > 0;
             iblockstate = worldIn.getBlockState(position)) {
            position = position.down();
        }

        if (worldIn.isAirBlock(position.up()))
            worldIn.setBlockState(position.up(), ModBlocks.LAMENT_STONE.getDefaultState(), 2);
        return true;
    }
}
