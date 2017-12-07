package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockCinnabarOre extends BlockOre {

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.CINNABAR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 4 + random.nextInt(5);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return MathHelper.getInt(new Random(), 2, 5);
    }
}
