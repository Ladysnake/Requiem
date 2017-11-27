package ladysnake.dissolution.common.capabilities;

import com.google.common.collect.ImmutableList;
import ladysnake.dissolution.api.ISoulHandler;
import ladysnake.dissolution.api.Soul;
import ladysnake.dissolution.api.SoulTypes;
import ladysnake.dissolution.common.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CapabilitySoulHandler {

    @CapabilityInject(ISoulHandler.class)
    public static Capability<ISoulHandler> CAPABILITY_SOUL_INVENTORY;

    public static void register() {
        CapabilityManager.INSTANCE.register(ISoulHandler.class, new Storage(), () -> new DefaultSoulInventoryHandler(1));
        MinecraftForge.EVENT_BUS.register(new CapabilitySoulHandler());
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer)
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "soul_inventory"), new CapabilitySoulHandler.Provider());
    }

    public static ISoulHandler getHandler(Entity entity) {

        if (entity.hasCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN))
            return entity.getCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);

        return null;
    }

    public static ISoulHandler getHandler(TileEntity te) {

        if (te.hasCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN))
            return te.getCapability(CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);

        return null;
    }

    public static class DefaultSoulInventoryHandler implements ISoulHandler {

        private final List<Soul> soulInventory = new ArrayList<>();
        private int size;

        /**
         * @param size the max number of souls able to be stored in this inventory
         */
        public DefaultSoulInventoryHandler(int size) {
            super();
            this.size = size;
        }

        @Override
        public boolean addSoul(Soul soul) {
            if (soulInventory.size() >= size)
                return false;
            this.soulInventory.add(soul);
            return true;
        }

        @Override
        public long getSoulCount(SoulTypes... filter) {
            List<SoulTypes> filterList = Arrays.asList(filter);
            return this.soulInventory.stream().filter(s -> filterList.isEmpty() || filterList.contains(s.getType())).count();
        }

        @Override
        public boolean removeSoul(Soul soul) {
            return this.soulInventory.remove(soul);
        }

        @Override
        public List<Soul> removeAll(SoulTypes... filter) {
            List<SoulTypes> filterList = Arrays.asList(filter);
            List<Soul> ret = soulInventory.stream().filter(s -> filterList.isEmpty() || filterList.contains(s.getType())).collect(Collectors.toList());
            this.soulInventory.removeAll(ret);
            return ret;
        }

        @Override
        public List<Soul> setSize(int size) {
            List<Soul> ret = new ArrayList<>();
            if (size < this.soulInventory.size())
                for (int i = size; i < this.soulInventory.size(); i++)
                    ret.add(this.soulInventory.remove(i));
            this.size = size;
            return ret;
        }

        @Override
        public ImmutableList<Soul> getSoulList() {
            return ImmutableList.copyOf(this.soulInventory);
        }

        @Override
        public String toString() {
            return "DefaultSoulInventoryHandler [soulInventory=" + soulInventory + "]";
        }

    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

        ISoulHandler instance = new DefaultSoulInventoryHandler(9);

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
            return capability == CAPABILITY_SOUL_INVENTORY;
        }

        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
            return hasCapability(capability, facing) ? CAPABILITY_SOUL_INVENTORY.cast(instance) : null;
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

    public static class Storage implements Capability.IStorage<ISoulHandler> {

        @Override
        public NBTBase writeNBT(Capability<ISoulHandler> capability, ISoulHandler instance,
                                EnumFacing side) {
            final NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            instance.getSoulList().forEach(s -> list.appendTag(s.writeToNBT()));
            tag.setTag("soulInventory", list);
            return tag;
        }

        @Override
        public void readNBT(Capability<ISoulHandler> capability, ISoulHandler instance,
                            EnumFacing side, NBTBase nbt) {
            final NBTTagCompound tag = (NBTTagCompound) nbt;
            NBTTagList list = tag.getTagList("soulInventory", 10);
            instance.removeAll();
            list.forEach(s -> instance.addSoul(new Soul((NBTTagCompound) s)));
        }

    }
}