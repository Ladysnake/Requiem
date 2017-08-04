package ladysnake.dissolution.common.blocks;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPowerConductor {

	static final PropertyBool ENABLED = PropertyBool.create("enabled");
	
	default void setActivated(World worldIn, BlockPos pos, boolean b) {}
	
	default boolean isActivated(World worldIn, BlockPos pos) {
		try {
			return worldIn.getBlockState(pos).getValue(ENABLED);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public static interface IEssentiaConductor {
		
	}
	
	public static interface IMachine extends IPowerConductor, IEssentiaConductor {
		
	}

}
