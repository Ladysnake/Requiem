package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IPowderContainer {

    default void pourPowder(GenericStackInventory<EnumPowderOres> powderStack) {
        GenericStackInventory.mergeInventories(powderStack, getPowderInventory());
    }

    NBTTagCompound saveToNbt(NBTTagCompound nbtTagCompound);

    default GenericStackInventory<EnumPowderOres> getPowderInventory() {
        return CapabilityGenericInventoryProvider.getInventory((ICapabilitySerializable) this, EnumPowderOres.class);
    }
}
