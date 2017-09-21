package ladysnake.dissolution.api;

import net.minecraft.nbt.NBTTagCompound;

/**
 * It's a stack, for essentia.
 * @author Pyrofab
 *
 */
public class EssentiaStack {
	
	public static final EssentiaStack EMPTY = new EssentiaStack(EssentiaTypes.UNTYPED, 0);

	/**
	 * The type of essentia contained in this stack. 
	 * Will be retained when this stack is emptied but can then be changed through merge operations.
	 */
	private EssentiaTypes type;
	private int stackSize;

	public EssentiaStack(EssentiaTypes type, int size) {
		super();
		this.type = type == null ? EssentiaTypes.UNTYPED : type;
		this.stackSize = size;
	}
	
	/**
	 * Constructor by copy
	 * @param toClone the essentia stack that this stack will copy
	 */
	public EssentiaStack(EssentiaStack toClone) {
		this(toClone.type, toClone.stackSize);
	}
	
	/**
	 * Reads an essentia stack from an NBTTagCompound
	 * @param compound
	 * @throws IllegalArgumentException if the provided tag is invalid
	 */
	public EssentiaStack(NBTTagCompound compound) {
		try {
			this.type = EssentiaTypes.valueOf(compound.getString("type"));
			if(this.type == null)
				this.type = EssentiaTypes.UNTYPED;
			this.stackSize = compound.getInteger("count");
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("The provided NBT compound did not contain the required information to create an essentia stack", e);
		}
	}
	
	/**
	 * @return the type of the essentia contained in this stack
	 */
	public EssentiaTypes getType() {
		return this.type;
	}
	
	/**
	 * @return the number of essentia units in this stack
	 */
	public int getCount() {
		return isEmpty() ? 0 : this.stackSize;
	}
	
	/**
	 * Sets the number of essentia units in this stack
	 * @param count
	 */
	public void setCount(int count) {
		this.stackSize = count;
	}
	
	/**
	 * Grows this stack by the given quantity
	 * @param quantity
	 * @return if this stack is too big, the quantity that could not be added
	 */
	public void grow(int quantity) {
		if(this.isEmpty())
			this.setCount(quantity);
        this.setCount(this.stackSize + quantity);
	}
	
	/**
	 * Adds one to this stack's count
	 */
	public void grow() {
		this.grow(1);
	}
	
	/**
	 * 
	 * @param quantity
	 * @return if this stack is too small, the quantity that could not be removed
	 */
	public void shrink(int quantity) {
		this.grow(-quantity);
	}
	
	public void shrink() {
		this.shrink(1);
	}
	
	public EssentiaStack withSize(int quantity) {
		return new EssentiaStack(this.type, quantity);
	}
	
	/**
	 * Equivalent to {@link #grow} but returns a new instance instead of modifying itself
	 * @param quantity
	 * @return
	 */
	public EssentiaStack bigger(int quantity) {
		return this.withSize(this.getCount() + quantity);
	}
	
	/**
	 * Equivalent to {@link #shrink} but returns a new instance instead of modifying itself
	 * @param quantity
	 * @return
	 */
	public EssentiaStack smaller(int quantity) {
		return this.withSize(this.getCount() - quantity);
	}
	
	/**
	 * Merges two essentia stacks
	 * @param stack
	 * @return true if the two stacks could be merged
	 */
	public boolean merge(EssentiaStack stack) {
		if(this.isEmpty())
			this.type = stack.type;
		else if (this.getType() != stack.getType())
			return false;
		this.grow(stack.getCount());
		return true;
	}
	
	public boolean isEmpty() {
		return this == EMPTY || this.type == EssentiaTypes.UNTYPED || this.stackSize <= 0;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("count", this.getCount());
		compound.setString("type", this.getType().name());
		return compound;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stackSize;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EssentiaStack other = (EssentiaStack) obj;
		if (stackSize != other.stackSize)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EssentiaStack [type=" + type + ", stackSize=" + stackSize + "]";
	}
	
}
