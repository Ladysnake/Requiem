package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Objects;

public class GenericStackInventory<T> implements INBTSerializable<NBTTagCompound> {
    private int maxSize;
    private INBTSerializableType.INBTTypeSerializer<T> serializer;
    private Class<T> typeClass;
    private final GenericStackList<T> stacks;

    public GenericStackInventory(int maxSize, int slotCount, Class<T> typeClass, INBTSerializableType.INBTTypeSerializer<T> serializer) {
        this.maxSize = maxSize;
        this.stacks = GenericStackList.withSize(slotCount);
        this.typeClass = typeClass;
        this.serializer = serializer;
    }

    public GenericStackInventory(GenericStackInventory<T> toClone) {
        this(toClone.maxSize, toClone.stacks.size(), toClone.typeClass, toClone.serializer);
        for(GenericStack<T> stack : toClone.stacks) {
            this.stacks.add(new GenericStack<>(stack));
        }
    }

    public Class<? extends T> getType() {
        return typeClass;
    }

    public int getSlotCount() {
        return this.stacks.size();
    }

    public int getTotalAmount() {
        return this.stacks.stream().filter(stack -> !stack.isEmpty()).mapToInt(GenericStack::getCount).sum();
    }

    @Nonnull
    public GenericStack<T> getStackInSlot(int slot) {
        return this.stacks.get(slot);
    }

    public GenericStack<T> readContent(T type) {
        return this.stacks.stream().filter(stack -> type == null || type.equals(stack.type)).findAny().orElse(GenericStack.empty());
    }

    public static <T> void mergeInventories(GenericStackInventory<T> fromInventory, GenericStackInventory<T> toInventory) {
        mergeLoop:
        for(GenericStack<T> stack : fromInventory.stacks) {
            for(GenericStack<T> stack2 : toInventory.stacks) {
                if(!fromInventory.canExtract() || !toInventory.canInsert()) return;
                stack2.merge(stack);
                if(stack.isEmpty())
                    continue mergeLoop;
            }
        }
    }

    public GenericStack<T> insert(GenericStack<T> stack) {
        if(!canInsert()) return stack;
        int i = this.stacks.indexOfType(stack.getType());
        if(i == -1)
            i = this.stacks.indexOfType(null);
        if(i > -1) {
            GenericStack<T> stack2 = this.stacks.get(i);
            int currentAmount = stack2.getCount();
            int amount = Math.min(stack.getCount() + currentAmount, maxSize);
            this.stacks.set(i, stack.withSize(amount));
            stack = stack.smaller(amount - currentAmount);
        }
        return stack;
    }

    private boolean canGenericStacksStack(GenericStack<T> a, GenericStack<T> b) {
        return !a.isEmpty() && a.getType().equals(b.getType());
    }

    public GenericStack<T> extract(int amount, T type) {
        if(!canExtract()) return GenericStack.empty();
        GenericStack<T> contentStack = this.stacks.get(type);
        if(contentStack.isEmpty())
            return GenericStack.empty();
        amount = Math.min(amount, contentStack.getCount());
        GenericStack<T> ret = contentStack.withSize(amount);
        contentStack.shrink(amount);
        return ret;
    }

    @Nonnull
    public GenericStack<T> extract(int slot, int amount, boolean simulate) {
        if (amount == 0 || !canExtract())
            return GenericStack.empty();

        validateSlotIndex(slot);

        GenericStack<T> existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return GenericStack.empty();

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, GenericStack.empty());
            }
            return existing;
        }
        else {
            if (!simulate) {
                this.stacks.set(slot, existing.withSize(existing.getCount() - toExtract));
            }

            return existing.withSize(toExtract);
        }
    }

    public int getSlotLimit(int slot) {
        return Math.min(this.maxSize, this.stacks.get(slot).getMaxStackSize());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("maxSize", this.maxSize);
        NBTTagList content = new NBTTagList();
        for(GenericStack<T> stack : this.stacks)
            content.appendTag(stack.writeToNBT(new NBTTagCompound(), serializer));
        compound.setTag("stacks", content);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList nbtContent = nbt.getTagList("stacks", 10);
        this.maxSize = nbt.getInteger("maxSize");
        stacks.clear();
        for(NBTBase compound : nbtContent)
            this.stacks.add(new GenericStack<>((NBTTagCompound)compound, serializer));
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    public boolean canInsert() {
        return true;
    }

    public boolean canExtract() {
        return true;
    }

    @Override
    public String toString() {
        return "GenericStackInventory{" +
                "maxSize=" + maxSize +
                ", typeClass=" + typeClass +
                ", stacks=" + stacks +
                '}';
    }
}
