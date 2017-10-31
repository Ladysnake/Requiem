package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import java.util.List;

public class TileEntityCrucible extends TileEntity implements IPowderContainer {

    private List<ItemStack> content = NonNullList.withSize(8, ItemStack.EMPTY);
    private GenericStackInventory<EnumPowderOres> powderInventory = new GenericStackInventory<>(8, 1, EnumPowderOres.class, EnumPowderOres.SERIALIZER);

    public void putItem(ItemStack itemStack) {
        this.content.add(itemStack);
    }

    @Override
    public GenericStackInventory<EnumPowderOres> getPowderInventory() {
        return powderInventory;
    }

    @Override
    public NBTTagCompound saveToNbt(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound;
    }

}
