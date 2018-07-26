package ladysnake.dissolution.api.corporeality;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IDeathStats extends INBTSerializable<NBTTagCompound> {

    BlockPos getDeathLocation();

    void setDeathLocation(BlockPos deathLocation);

    int getDeathDimension();

    void setDeathDimension(int dimension);

}
