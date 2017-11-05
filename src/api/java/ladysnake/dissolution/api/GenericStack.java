package ladysnake.dissolution.api;

import jline.internal.Nullable;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * It's a stack, for essentia.
 * @author Pyrofab
 *
 */
public class GenericStack<T> {

	public static final GenericStack EMPTY = new GenericStack<>(null, 0, Integer.MAX_VALUE);
	private int maxStackSize;

	@SuppressWarnings("unchecked")
	public static <T> GenericStack<T> empty() {
		return EMPTY;
	}

	/**
	 * The type contained in this stack.
	 * Will be retained when this stack is emptied but can then be changed through merge operations.
	 */
	protected T type;
	protected int stackSize;

	public GenericStack(T type) {
		this(type, 1);
	}

	public GenericStack(T type, int size) {
		this(type, size, 64);
	}

	public GenericStack(T type, int size, int maxStackSize) {
		super();
		this.type = type;
		this.stackSize = size % maxStackSize;
		this.maxStackSize = maxStackSize;
	}

	/**
	 * Constructor by copy
	 * @param toClone the stack that this stack will copy
	 */
	public GenericStack(GenericStack<T> toClone) {
		this(toClone.type, toClone.stackSize, toClone.maxStackSize);
	}

	/**
	 * Reads a stack from an NBTTagCompound
	 */
	public GenericStack(NBTTagCompound compound, INBTSerializableType.INBTTypeSerializer<T> deserializer) {
		this(compound, deserializer::deserialize);
	}

	public GenericStack(NBTTagCompound compound, Function<NBTTagCompound, T> deserializer) {
		this(deserializer.apply(compound), compound.getInteger("count"), compound.getInteger("maxCount"));
	}

	/**
	 * @return the type of this stack
	 */
	@Nullable
	public T getType() {
		return this.type;
	}
	
	/**
	 * @return the number of units in this stack
	 */
	public int getCount() {
		return isEmpty() ? 0 : this.stackSize;
	}
	
	/**
	 * Sets the number of units contained in this stack
	 */
	public int setCount(int count) {
		int remainder = Math.max(0, count - maxStackSize);
		this.stackSize = count - remainder;
		return remainder;
	}
	
	/**
	 * Grows this stack by the given quantity
	 * @return the remainder of the operation
	 */
	public int grow(int quantity) {
		if(this.isEmpty())
			this.setCount(quantity);
        return this.setCount(this.stackSize + quantity);
	}
	
	/**
	 * Adds one to this stack's count
	 * @return true if the stack size has been incremented
	 */
	public boolean grow() {
		return this.grow(1) == 0;
	}

	public int shrink(int quantity) {
		return this.grow(-quantity);
	}

	/**
	 * Removes one to this stack's count
	 * @return true if the stack size has been decremented
	 */
	public boolean shrink() {
		return this.shrink(1) == 0;
	}
	
	public GenericStack<T> withSize(int quantity) {
		return new GenericStack<>(this.type, quantity, this.maxStackSize);
	}
	
	/**
	 * Equivalent to {@link #grow} but returns a new instance instead of modifying itself
	 */
	public GenericStack<T> bigger(int quantity) {
		return this.withSize(this.getCount() + quantity);
	}
	
	/**
	 * Equivalent to {@link #shrink} but returns a new instance instead of modifying itself
	 */
	public GenericStack<T> smaller(int quantity) {
		return this.withSize(this.getCount() - quantity);
	}
	
	/**
	 * Merges two stacks
	 * @return true if the two stacks could be merged
	 */
	public boolean merge(GenericStack<T> stack) {
		if(stack.isEmpty()) return true;
		if(this.isEmpty())
			this.type = stack.type;
		else if (this.getType() != stack.getType())
			return false;
		stack.setCount(this.grow(stack.getCount()));
		return true;
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}

	public boolean isEmpty() {
		return this == EMPTY || this.type == null || this.stackSize <= 0;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if(type instanceof INBTSerializableType)
			return writeToNBT(compound, ((INBTSerializableType<T>) type).getSerializer());
		throw new UnsupportedOperationException(this.type.getClass() + " has no default serializer");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound, INBTSerializableType.INBTTypeSerializer<T> serializer) {
		return writeToNBT(compound, serializer::serialize);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound, BiConsumer<T, NBTTagCompound> serializer) {
		compound.setInteger("count", this.getCount());
		compound.setInteger("maxCount", this.getMaxStackSize());
		serializer.accept(this.getType(), compound);
		return compound;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GenericStack<?> that = (GenericStack<?>) o;

		if (maxStackSize != that.maxStackSize) return false;
		//noinspection SimplifiableIfStatement
		if (stackSize != that.stackSize) return false;
		return type != null ? type.equals(that.type) : that.type == null;
	}

	@Override
	public int hashCode() {
		int result = maxStackSize;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + stackSize;
		return result;
	}

	@Override
	public String toString() {
		return "GenericStack{" +
				"type=" + type +
				", stackSize=" + stackSize +
				'}';
	}
}
