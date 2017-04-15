package ladysnake.tartaros.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class IncorporealStorage implements IStorage<IIncorporeal> {

	@Override
	public NBTBase writeNBT(Capability<IIncorporeal> capability, IIncorporeal instance, EnumFacing side) {
		return new NBTTagByte((byte) (instance.isIncorporeal() ? 1 : 0));
	}

	@Override
	public void readNBT(Capability<IIncorporeal> capability, IIncorporeal instance, EnumFacing side, NBTBase nbt) {
		instance.setIncorporeal(((NBTPrimitive) nbt).getByte() == 1);
	}

}
