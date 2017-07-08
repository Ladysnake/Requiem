package ladysnake.dissolution.common.world;

import java.util.Random;

import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.init.ModFluids;
import ladysnake.dissolution.common.world.gen.feature.WorldGenMercuryLakes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGen implements IWorldGenerator {
	
	private WorldGenLakes mercuryLakesGenerator = new WorldGenLakes(ModFluids.MERCURY.fluidBlock());

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		
		switch (world.provider.getDimension()) {
		case -1:
			generateNether(world, chunkX * 16, chunkZ * 16, random);
			break;

		case 0:
			generateOverWorld(world, chunkX * 16, chunkZ * 16, random);
			break;

		case 1:
			generateEnd(world, chunkX * 16, chunkZ * 16, random);
			break;
		}

	}



	private void generateNether(World world, int i, int j, Random random) {

	}

	private void generateOverWorld(World world, int i, int j, Random random) {
		
		int Xpos = i + random.nextInt(8);
		int Ypos = random.nextInt(128);
		int Zpos = j + random.nextInt(8);
		
		/*CANDLE*/
		//new StructureCandle().generate(world, new BlockPos(Xpos, Ypos, Zpos), random);
		
		/*MERCURY_LAKES*/
		if(DissolutionConfig.spawnMercuryLakesFreq > 0 && random.nextInt(DissolutionConfig.spawnMercuryLakesFreq) == 0)
			mercuryLakesGenerator.generate(world, random, new BlockPos(Xpos, Ypos, Zpos));
		
	}
		
	private void generateEnd(World world, int i, int j, Random random) {

	}

}
