package ladysnake.dissolution.common.capabilities;

import java.util.Arrays;
import java.util.List;

import ladysnake.dissolution.api.DistillateTypes;

import ladysnake.dissolution.api.DistillateStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class DistillateList extends NonNullList<DistillateStack> {
	
	public static DistillateList withSize(int size) {
        DistillateStack[] aobject = new DistillateStack[size];
        Arrays.fill(aobject, DistillateStack.EMPTY);
        return new DistillateList(Arrays.asList(aobject));
	}
	
	protected DistillateList(List<DistillateStack> delegateIn) {
		super(delegateIn, DistillateStack.EMPTY);
	}
	
	public DistillateStack get(DistillateTypes type) {
		return this.stream().filter(stack -> !stack.isEmpty() && (type == DistillateTypes.UNTYPED || stack.getType() == type)).findAny().orElse(DistillateStack.EMPTY);
	}
	
	/**
	 * 
	 * @param type
	 * @return the index of an essentia stack of the requested type
	 */
	public int indexOf(DistillateTypes type) {
		int index = -1;
		for(int i = 0; i < this.size() && index == -1; i++)
			if((get(i).isEmpty() && type == DistillateTypes.UNTYPED) || get(i).getType() == type)
				index = i;
		return index;
	}
	
	public boolean contains(DistillateTypes type) {
		return this.stream().anyMatch(e -> e.getType() == type);
	}
	
	public boolean add(DistillateStack stack) {
		for(int i = 0; i < this.size(); i++)
			if(this.get(i).isEmpty()) {
				this.set(i, stack);
				return true;
			}
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		return this.stream().allMatch(e -> e.isEmpty());
	}
	
	@Override
	public boolean remove(Object arg0) {
		int index = indexOf(arg0);
		if(index == -1)
			return false;
		this.set(index, DistillateStack.EMPTY);
		return true;
	}
	
	@Override
	@Nonnull
	public DistillateStack remove(int index) {
		return this.set(index, DistillateStack.EMPTY);
	}

}
