package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.nbt.NBTTagCompound;

public interface IPowderContainer {

    default void pourPowder(GenericStackInventory<EnumPowderOres> powderStack) {
        GenericStackInventory.mergeInventories(powderStack, getPowderInventory());
    }

    NBTTagCompound saveToNbt(NBTTagCompound nbtTagCompound);

    GenericStackInventory<EnumPowderOres> getPowderInventory();
}
