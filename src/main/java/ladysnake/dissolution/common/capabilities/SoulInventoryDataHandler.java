package ladysnake.dissolution.common.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class SoulInventoryDataHandler {
	
	@CapabilityInject(ISoulInventoryHandler.class)
	public static final Capability<ISoulInventoryHandler> CAPABILITY_SOUL_INVENTORY = null;
	
	public static void register() {
        CapabilityManager.INSTANCE.register(ISoulInventoryHandler.class, new Storage(), DefaultSoulInventoryHandler.class);
        MinecraftForge.EVENT_BUS.register(new SoulInventoryDataHandler());
    }
    
    public static ISoulInventoryHandler getHandler(Entity entity) {

        if (entity.hasCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN))
            return entity.getCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);
        
        return null;
    }
    
    public static ISoulInventoryHandler getHandler(TileEntity te) {

        if (te.hasCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN))
            return te.getCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);
        
        return null;
    }

	public static class DefaultSoulInventoryHandler implements ISoulInventoryHandler {
		
		private final List<Soul> soulInventory = new ArrayList<>(MAX_SOULS);
		public static final int MAX_SOULS = 9;
	
		@Override
		public boolean addSoul(Soul soul) {
			if(soulInventory.size() > MAX_SOULS)
				return false;
			this.soulInventory.add(soul);
			return true;
		}

		@Override
		public int getSoulCount() {
			return this.soulInventory.size();
		}

		@Override
		public int getSoulCount(SoulTypes soultype) {
			return (int)this.soulInventory.stream().filter(s -> s.getType() == soultype).count();
		}
	
		@Override
		public boolean removeSoul(Soul soul) {
			return this.soulInventory.remove(soul);
		}

		@Override
		public List<Soul> removeAll(SoulTypes type) {
			List<Soul> ret = soulInventory.stream().filter(s -> s.getType() == type).collect(Collectors.toList());
			this.soulInventory.removeAll(ret);
			return ret;
		}
		
		@Override
		public void forEach(Consumer<Soul> action) {
			for(Soul s : this.soulInventory)
				action.accept(s);
		}

		@Override
		public Soul get(Predicate<Soul> condition) {
			return this.soulInventory.stream().filter(condition).findFirst().orElse(Soul.UNDEFINED);
		}
		
		@Override
		public List<Soul> getSoulList() {
			return this.soulInventory;
		}
	
	}
	
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		ISoulInventoryHandler instance = CAPABILITY_SOUL_INVENTORY.getDefaultInstance();
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CAPABILITY_SOUL_INVENTORY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? CAPABILITY_SOUL_INVENTORY.<T>cast(instance) : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) CAPABILITY_SOUL_INVENTORY.getStorage().writeNBT(CAPABILITY_SOUL_INVENTORY, instance, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			CAPABILITY_SOUL_INVENTORY.getStorage().readNBT(CAPABILITY_SOUL_INVENTORY, instance, null, nbt);
		}
		
	}
	
	public static class Storage implements Capability.IStorage<ISoulInventoryHandler> {

		@Override
		public NBTBase writeNBT(Capability<ISoulInventoryHandler> capability, ISoulInventoryHandler instance,
				EnumFacing side) {
			final NBTTagCompound tag = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			instance.forEach(s -> list.appendTag(s.writeToNBT()));
			tag.setTag("soulInventory", list);
			return tag;
		}

		@Override
		public void readNBT(Capability<ISoulInventoryHandler> capability, ISoulInventoryHandler instance,
				EnumFacing side, NBTBase nbt) {
			final NBTTagCompound tag = (NBTTagCompound) nbt;
			NBTTagList list = tag.getTagList("soulInventory", 10);
			list.forEach(s -> instance.addSoul(Soul.readFromNBT((NBTTagCompound) s)));
		}
		
	}
}