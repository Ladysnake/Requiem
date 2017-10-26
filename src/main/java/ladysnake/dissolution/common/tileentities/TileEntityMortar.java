package ladysnake.dissolution.common.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TileEntityMortar extends TileEntity {

    private int crushTime;
    private ItemStack contentStack = ItemStack.EMPTY;
    private ItemStack crushedStack = ItemStack.EMPTY;

    public void putItem(ItemStack item) {
        if(this.contentStack.isEmpty()) {
            this.contentStack = item.splitStack(1);
            this.crushTime = this.world.rand.nextInt(12) + 12;
        }
    }

    public void crush() {
        if(crushTime-- == 0) {
            crushedStack = contentStack;
            contentStack = ItemStack.EMPTY;
        }
    }

    private void loadFromNbt(NBTTagCompound compound) {
        if(compound.hasKey("crushedContent"))
            this.crushedStack = new ItemStack(compound.getCompoundTag("crushedContent"));
    }

    public NBTTagCompound saveToNbt(NBTTagCompound compound) {
        compound.setTag("crushedContent", this.crushedStack.serializeNBT());
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
