package ladysnake.dissolution.common.blocks.alchemysystem;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPowerConductor {

	static final PropertyBool POWERED = PropertyBool.create("powered");
	
	/**
	 * Changes the powered status of the target block
	 * @param worldIn
	 * @param pos
	 * @param powered
	 */
	default void setPowered(World worldIn, BlockPos pos, boolean powered) {}
	
	/**
	 * @param worldIn
	 * @param pos
	 * @return true if the block at this position is powered by a power core or equivalent
	 */
	default boolean isPowered(World worldIn, BlockPos pos) {
		try {
			return worldIn.getBlockState(pos).getValue(POWERED);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	/**
	 * @param worldIn
	 * @param pos
	 * @return Whether the block at this position is conductive in its current state
	 */
	default boolean isConductive(World worldIn, BlockPos pos) {
			return true;
	}
	
	public static interface IEssentiaConductor {}
	
	public static interface IMachine extends IPowerConductor, IEssentiaConductor {
		
		default boolean shouldConnect(IBlockState state) {
			return true;
		}
		
	}

}
