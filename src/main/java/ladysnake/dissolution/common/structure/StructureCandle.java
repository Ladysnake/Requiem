package ladysnake.dissolution.common.structure;

import java.util.Random;

import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureCandle {

	public boolean generate(World world, BlockPos pos, Random random) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (world.getBlockState(new BlockPos(x, y - 1, z)) == Blocks.GRASS.getDefaultState()) {
			
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.SULFUR_CANDLE.getDefaultState());
			
		}
		
		else if (world.getBlockState(new BlockPos(x, y - 1, z)) == Blocks.STONE.getDefaultState() && world.getBlockState(new BlockPos(x, y + 1, z)) == Blocks.AIR.getDefaultState()){
			
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.MERCURY_CANDLE.getDefaultState());
			
		}
		
		return true;
	}

}
