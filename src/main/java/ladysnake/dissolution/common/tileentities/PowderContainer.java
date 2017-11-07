package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.api.INBTSerializableType;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PowderContainer extends TileEntity {
    protected IGenericInventoryProvider inventoryProvider = new CapabilityGenericInventoryProvider.DefaultGenericInventoryProvider();
    protected GenericStackInventory<EnumPowderOres> powderInventory = new PowderInventory(EnumPowderOres.class, EnumPowderOres.SERIALIZER);
    private boolean shouldDrop = true;
    protected InputItemHandler itemInventory = new PowderContainer.ItemHandler(Blocks.CLAY, Blocks.MAGMA, Blocks.COAL_BLOCK);

    public PowderContainer() {
        super();
        inventoryProvider.setInventory(EnumPowderOres.class, powderInventory);
    }

    public boolean pourPowder(GenericStackInventory<EnumPowderOres> powderStack) {
        GenericStack<EnumPowderOres> stack = powderStack.extract(getMaxVolume(), null);
        if(stack.isEmpty())
            return false;
        GenericStack<EnumPowderOres> tilePowder = getPowderInventory().insert(stack);
        powderStack.insert(tilePowder);
        return tilePowder.getCount() != stack.getCount();
    }

    public boolean shouldDrop() {
        return shouldDrop;
    }

    public void setShouldDrop(boolean shouldDrop) {
        this.shouldDrop = shouldDrop;
    }

    public ItemStack getContent() {
        return this.itemInventory.getStackInSlot(0);
    }

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public void dropContent() {
        IItemHandler itemInventory = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        BlockPos pos = this.getPos();
        if(itemInventory != null)
            for(int i = 0; i < itemInventory.getSlots(); i++)
                this.getWorld().spawnEntity(new EntityItem(this.getWorld(), pos.getX(), pos.getY(), pos.getZ(), itemInventory.getStackInSlot(i)));

    }

    public NBTTagCompound saveToNbt(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("powderInventory", powderInventory.serializeNBT());
        compound.setTag("itemInventory", itemInventory.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("powderInventory"))
            this.powderInventory.deserializeNBT(compound.getCompoundTag("powderInventory"));
        if(compound.hasKey("itemInventory"))
            this.itemInventory.deserializeNBT(compound.getCompoundTag("itemInventory"));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if(!world.isRemote) {
            ChunkPos cp = this.world.getChunkFromBlockCoords(getPos()).getPos();
            PlayerChunkMapEntry entry = ((WorldServer)this.world).getPlayerChunkMap().getEntry(cp.x, cp.z);
            if (entry!=null) {
                entry.sendPacket(this.getUpdatePacket());
            }
        }
    }

    public GenericStackInventory<EnumPowderOres> getPowderInventory() {
        return CapabilityGenericInventoryProvider.getInventory(this, EnumPowderOres.class);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC)
            return CapabilityGenericInventoryProvider.CAPABILITY_GENERIC.cast(inventoryProvider);
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemInventory);
        return super.getCapability(capability, facing);
    }

    protected abstract int getMaxVolume();

    protected abstract boolean isFull();

    protected void onPowderInsertion(GenericStack<EnumPowderOres> stack) {}

    protected void onItemInsertion(ItemStack stack) {}

    class PowderInventory extends GenericStackInventory<EnumPowderOres> {

        private PowderInventory(Class<EnumPowderOres> typeClass, INBTSerializableType.INBTTypeSerializer<EnumPowderOres> serializer) {
            super(PowderContainer.this.getMaxVolume(), PowderContainer.this.getMaxVolume(), typeClass, serializer);
        }

        @Override
        public int getSlotLimit(int slot) {
            return super.getSlotLimit(slot);// - PowderContainer.this.fluidInventory.getFluidAmount();
        }

        @Override
        public boolean canInsert() {
            return !PowderContainer.this.isFull() && super.canInsert();
        }

        @Override
        public GenericStack<EnumPowderOres> insert(GenericStack<EnumPowderOres> stack) {
            onPowderInsertion(stack);
            GenericStack<EnumPowderOres> ret = super.insert(stack);
            markDirty();
            return ret;
        }
    }

    class ItemHandler extends InputItemHandler {
        public ItemHandler(Block... whitelist) {
            super(whitelist);
            this.setMaxSize(getMaxVolume());
        }

        public ItemHandler(Item... whitelist) {
            super(whitelist);
            this.setMaxSize(getMaxVolume());
        }

        @Override
        public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if(PowderContainer.this.isFull())
                return stack;
            onItemInsertion(stack);
            ItemStack ret = super.insertItemInternal(slot, stack, simulate);
            markDirty();
            return ret;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if(PowderContainer.this.isFull())
                return stack;
            onItemInsertion(stack);
            //noinspection StatementWithEmptyBody   -- inserts items one by one until full
            while(!PowderContainer.this.isFull() && !stack.isEmpty() && super.insertItem(slot, stack.splitStack(1), simulate).isEmpty());
            markDirty();
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack ret = super.extractItem(slot, amount, simulate);
            markDirty();
            return ret;
        }
    }
}
