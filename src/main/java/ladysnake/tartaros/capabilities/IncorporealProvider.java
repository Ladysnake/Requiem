package ladysnake.tartaros.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class IncorporealProvider implements ICapabilitySerializable<NBTBase> {
	
	@CapabilityInject(IIncorporeal.class)
	public static final Capability<IIncorporeal> INCORPOREAL_CAP = null;
	
	private IIncorporeal instance = INCORPOREAL_CAP.getDefaultInstance();

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == INCORPOREAL_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == INCORPOREAL_CAP ? INCORPOREAL_CAP.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return INCORPOREAL_CAP.getStorage().writeNBT(INCORPOREAL_CAP, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		INCORPOREAL_CAP.getStorage().readNBT(INCORPOREAL_CAP, this.instance, null, nbt);		
	}
	
}
