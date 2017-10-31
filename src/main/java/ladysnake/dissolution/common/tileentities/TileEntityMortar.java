package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TileEntityMortar extends TileEntity implements IPowderContainer {

    public static final Map<Item, EnumPowderOres> itemToPowder = new HashMap<>();

    private int crushTime;
    private ItemStack contentStack = ItemStack.EMPTY;
    private GenericStackInventory<EnumPowderOres> crushedStack = new GenericStackInventory<>(8, 1, EnumPowderOres.class, EnumPowderOres.SERIALIZER);

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
