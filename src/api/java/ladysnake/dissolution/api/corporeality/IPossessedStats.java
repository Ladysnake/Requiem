package ladysnake.dissolution.api.corporeality;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPossessedStats extends INBTSerializable<NBTTagCompound> {

    int getPurifiedHealth();

    void purifyHealth(int purified);

    void setPurifiedHealth(int health);
}
