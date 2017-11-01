package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TileEntityMortar extends TileEntity implements IPowderContainer {

    public static final Map<Item, EnumPowderOres> itemToPowder = new HashMap<>();

    private int crushTime;
    private ItemStack contentStack = ItemStack.EMPTY;
    private IGenericInventoryProvider inventoryProvider = new CapabilityGenericInventoryProvider.DefaultGenericInventoryProvider();
    private GenericStackInventory<EnumPowderOres> crushedStack = new GenericStackInventory<>(8, 1, EnumPowderOres.class, EnumPowderOres.SERIALIZER);

    public TileEntityMortar() {
        inventoryProvider.setInventory(EnumPowderOres.class, crushedStack);
    }

    public void putItem(ItemStack item) {
        if(this.contentStack.isEmpty()) {
            this.contentStack = item.splitStack(1);
            this.crushTime = this.world.rand.nextInt(12) + 12;
        }
    }

    public void crush() {
        if(crushTime-- == 0) {
            crushedStack.insert(Arrays.stream(EnumPowderOres.values())
                    .filter(enumPowders -> enumPowders.getComponent().equals(contentStack.getItem()))
                    .map(GenericStack::new).findAny().orElse(GenericStack.empty()));
            contentStack = ItemStack.EMPTY;
        }
    }

    @Override
    public GenericStackInventory<EnumPowderOres> getPowderInventory() {
        return crushedStack;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC)
            return CapabilityGenericInventoryProvider.CAPABILITY_GENERIC.cast(inventoryProvider);
        return super.getCapability(capability, facing);
    }

    private void loadFromNbt(NBTTagCompound compound) {
        if(compound.hasKey("crushedContent"))
            this.crushedStack.deserializeNBT(compound.getCompoundTag("crushedContent"));
    }

    public NBTTagCompound saveToNbt(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        loadFromNbt(compound);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return saveToNbt(super.writeToNBT(compound));
    }

}
