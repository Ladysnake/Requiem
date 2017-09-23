package ladysnake.dissolution.common.capabilities;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;

import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.EssentiaTypes;
import net.minecraft.util.NonNullList;

public class EssentiaList extends NonNullList<EssentiaStack> {
	
	public static EssentiaList withSize(int size) {
        EssentiaStack[] aobject = new EssentiaStack[size];
        Arrays.fill(aobject, EssentiaStack.EMPTY);
        return new EssentiaList(Arrays.asList(aobject));
	}
	
	protected EssentiaList(List<EssentiaStack> delegateIn) {
		super(delegateIn, EssentiaStack.EMPTY);
	}
	
	public EssentiaStack get(EssentiaTypes type) {
		return this.stream().filter(stack -> !stack.isEmpty() && (type == EssentiaTypes.UNTYPED || stack.getType() == type)).findAny().orElse(EssentiaStack.EMPTY);
	}
	
	/**
	 * 
	 * @param type
	 * @return the index of an essentia stack of the requested type
	 */
	public int indexOf(EssentiaTypes type) {
		int index = -1;
		for(int i = 0; i < this.size() && index == -1; i++)
			if((get(i).isEmpty() && type == EssentiaTypes.UNTYPED) || get(i).getType() == type)
				index = i;
		return index;
	}
	
	public boolean contains(EssentiaTypes type) {
		return this.stream().anyMatch(e -> e.getType() == type);
	}
	
	public boolean add(EssentiaStack stack) {
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
		this.set(index, EssentiaStack.EMPTY);
		return true;
	}
	
	@Override
	public EssentiaStack remove(int index) {
		return this.set(index, EssentiaStack.EMPTY);
	}

}
