package ladysnake.dissolution.common.world;

import java.util.Random;

import ladysnake.dissolution.common.Reference.Blocks;
import ladysnake.dissolution.common.structure.StructureCandle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGen implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		
		switch (world.provider.getDimension()) {
		case -1:
			GenerateNether(world, chunkX * 16, chunkZ * 16, random);
			break;

		case 0:
			GenerateOverWorld(world, chunkX * 16, chunkZ * 16, random);
			break;

		case 1:
			GenerateEnd(world, chunkX * 16, chunkZ * 16, random);
			break;
		}

	}



	private void GenerateNether(World world, int i, int j, Random random) {

	}

	private void GenerateOverWorld(World world, int i, int j, Random random) {
		
		//Structure
		int Xpos = i + random.nextInt(16);
		int Ypos = random.nextInt(128);
		int Zpos = j + random.nextInt(16);
		
		new StructureCandle().generate(world, new BlockPos(Xpos, Ypos, Zpos), random);
		
	}
		
	private void GenerateEnd(World world, int i, int j, Random random) {

	}

}
