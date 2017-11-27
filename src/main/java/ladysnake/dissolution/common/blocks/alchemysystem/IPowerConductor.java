package ladysnake.dissolution.common.blocks.alchemysystem;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IPowerConductor {

    PropertyBool POWERED = PropertyBool.create("powered");

    /**
     * Changes the powered status of the target block
     *
     * @param worldIn
     * @param pos
     * @param powered
     */
    default void setPowered(IBlockAccess worldIn, BlockPos pos, boolean powered) {
    }

    /**
     * @param worldIn
     * @param pos
     * @return true if the block at this position is powered by a power core or equivalent
     */
    default boolean isPowered(IBlockAccess worldIn, BlockPos pos) {
        try {
            return worldIn.getBlockState(pos).getValue(POWERED);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the block at this position accepts a connection
     *
     * @param pos    the position of the block being checked
     * @param facing the face on which the connection is attempted
     * @return true if there should be a connection
     */
    default boolean shouldPowerConnect(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
        return true;
    }

    /**
     * @param worldIn
     * @param pos
     * @return Whether the block at this position is conductive in its current state
     */
    default boolean isConductive(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    interface IMachine extends IPowerConductor {

        PowerConsumption getPowerConsumption(IBlockAccess worldIn, BlockPos pos);

        boolean shouldPowerConnect(IBlockAccess worldIn, BlockPos pos, EnumFacing facing);

        enum PowerConsumption {
            CONSUMER,
            GENERATOR,
            NONE
        }

    }

}
