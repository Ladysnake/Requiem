package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public class GenericStackInventory<T> implements INBTSerializable<NBTTagCompound>, Iterable<GenericStack<T>> {
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

    public Class<? extends T> getType() {
        return typeClass;
    }

    public int getSlots() {
        return this.stacks.size();
    }

    @Nonnull
    public GenericStack<T> getStackInSlot(int slot) {
        return this.stacks.get(slot);
    }

    public static <T> void mergeInventories(GenericStackInventory<T> from, GenericStackInventory<T> to) {
        mergeLoop:
        for(GenericStack<T> stack : from) {
            for(GenericStack<T> stack2 : to) {
                stack2.merge(stack);
                if(stack.isEmpty())
                    continue mergeLoop;
            }
        }
    }

    public GenericStack<T> insert(GenericStack<T> stack) {
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

    @Nonnull
    public GenericStack<T> insert(int slot, @Nonnull GenericStack<T> stack, boolean simulate) {
        if(stack.isEmpty())
            return GenericStack.empty();
        validateSlotIndex(slot);

        GenericStack<T> existing = this.stacks.get(slot);

        int limit = this.maxSize;

        if (!existing.isEmpty()) {
            if (!canGenericStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? stack.withSize(limit) : stack);
            }
            else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? stack.withSize(stack.getCount()- limit) : GenericStack.empty();
    }

    private boolean canGenericStacksStack(GenericStack<T> a, GenericStack<T> b) {
        return !a.isEmpty() && a.getType().equals(b.getType());
    }

    public GenericStack<T> extract(int amount, T type) {
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
        if (amount == 0)
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
            this.stacks.add(new GenericStack<>(serializer.deserialize((NBTTagCompound) compound), nbt.getInteger("count")));
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    @Nonnull
    @Override
    public Iterator<GenericStack<T>> iterator() {
        return this.stacks.iterator();
    }
}
