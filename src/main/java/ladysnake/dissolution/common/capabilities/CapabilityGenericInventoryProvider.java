package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.Reference;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CapabilityGenericInventoryProvider {

    @CapabilityInject(IGenericInventoryProvider.class)
    public static Capability<IGenericInventoryProvider> CAPABILITY_GENERIC;

    public static void register() {
        CapabilityManager.INSTANCE.register(IGenericInventoryProvider.class, new Storage(), DefaultGenericInventoryProvider::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> GenericStackInventory<T> getInventory(ICapabilitySerializable capabilitySerializable, Class<T> tClass) {
        if (capabilitySerializable.hasCapability(CAPABILITY_GENERIC, null)) {
            IGenericInventoryProvider handler = capabilitySerializable.getCapability(CAPABILITY_GENERIC, null);
            return handler != null && handler.hasInventoryFor(tClass) ? handler.getInventoryFor(tClass) : null;
        }
        return null;
    }

    public static class DefaultGenericInventoryProvider implements IGenericInventoryProvider {
        private Map<Class, GenericStackInventory> inventoryMap = new HashMap<>();

        @Override
        public <T> void setInventory(Class<T> clazz, GenericStackInventory<T> inventory) {
            inventoryMap.put(clazz, inventory);
        }

        @Override
        public boolean hasInventoryFor(Class clazz) {
            return inventoryMap.containsKey(clazz);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> GenericStackInventory<T> getInventoryFor(Class<T> clazz) {
            return inventoryMap.get(clazz);
        }

        @Nonnull
        @Override
        public Iterator<Map.Entry<Class, GenericStackInventory>> iterator() {
            return this.inventoryMap.entrySet().iterator();
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTBase> {
        IGenericInventoryProvider instance;

        public Provider() {
            this.instance = new DefaultGenericInventoryProvider();
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability.equals(CAPABILITY_GENERIC);
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability.equals(CAPABILITY_GENERIC))
                return CAPABILITY_GENERIC.cast(instance);
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return CAPABILITY_GENERIC.getStorage().writeNBT(CAPABILITY_GENERIC, instance, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            CAPABILITY_GENERIC.getStorage().readNBT(CAPABILITY_GENERIC, instance, null, nbt);
        }
    }

    public static class Storage implements Capability.IStorage<IGenericInventoryProvider> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IGenericInventoryProvider> capability, IGenericInventoryProvider instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList inventories = new NBTTagList();
            for (Map.Entry<Class, GenericStackInventory> entry : instance) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("type", EnumSerializableTypes.forClass(entry.getKey()).name());
                nbt.setInteger("slotCount", entry.getValue().getSlotCount());
                nbt.setTag("inventory", entry.getValue().serializeNBT());
                inventories.appendTag(nbt);
            }
            compound.setTag("inventories", inventories);
            return compound;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readNBT(Capability<IGenericInventoryProvider> capability, IGenericInventoryProvider instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagCompound) {
                NBTTagList inventories = ((NBTTagCompound) nbt).getTagList("inventories", 10);
                for (NBTBase inventoryNBT : inventories) {
                    try {
                        EnumSerializableTypes type = EnumSerializableTypes.valueOf(((NBTTagCompound) inventoryNBT).getString("type"));
                        int slotCount = ((NBTTagCompound) inventoryNBT).getInteger("slotCount");
                        GenericStackInventory inventory = new GenericStackInventory<>(0, slotCount, type.clazz, type.serializer);
                        inventory.deserializeNBT(((NBTTagCompound) inventoryNBT).getCompoundTag("inventory"));
                        instance.setInventory(type.clazz, inventory);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
