package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class InventoryPlayerCorpse implements IInventory {

    /**
     * An array of 36 item stacks indicating the main player inventory (including the visible bar).
     */
    private NonNullList<ItemStack> mainInventory = NonNullList.withSize(36, ItemStack.EMPTY);
    private EntityPlayerCorpse corpseEntity;
    private boolean dirty;

    public InventoryPlayerCorpse(EntityPlayerCorpse corpse) {
        this(NonNullList.withSize(36, ItemStack.EMPTY), corpse);
    }

    public InventoryPlayerCorpse(NonNullList<ItemStack> inv, EntityPlayerCorpse corpse) {
        super();
        for (int i = 0; i < inv.size(); i++)
            this.mainInventory.set(i, inv.get(i));
        this.corpseEntity = corpse;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.corpseEntity.getName() + "'s stuff";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }

    @Override
    public int getSizeInventory() {
        return this.mainInventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.mainInventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.mainInventory.get(index);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return !(this.mainInventory.get(index)).isEmpty() ? ItemStackHelper.getAndSplit(this.mainInventory, index, count) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index) {
        try {
            ItemStack ret = this.mainInventory.get(index);
            this.mainInventory.set(index, ItemStack.EMPTY);
            return ret;
        } catch (ArrayIndexOutOfBoundsException e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        this.mainInventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return this.corpseEntity.getDistanceSq(player) < 50;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.mainInventory.clear();
    }

    public void dropAllItems(Entity entityIn) {
        mainInventory.forEach(stack -> entityIn.entityDropItem(stack, 0.5f));
    }

    public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            if (!this.mainInventory.get(i).isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.mainInventory.get(i).writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }
        return nbtTagListIn;
    }

    public void readFromNBT(NBTTagList nbtTagListIn) {
        this.mainInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 0xFF;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty()) {
                if (j < this.mainInventory.size()) {
                    this.mainInventory.set(j, itemstack);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "InventoryPlayerCorpse [mainInventory=" + mainInventory + ", dirty="
                + dirty + "]";
    }

}
