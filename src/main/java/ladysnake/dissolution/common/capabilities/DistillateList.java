package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.api.GenericStackList;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DistillateList extends GenericStackList<DistillateTypes> {

    public static DistillateList withSize(int size) {
        DistillateStack[] genericStacks = new DistillateStack[size];
        Arrays.fill(genericStacks, GenericStack.empty());
        return new DistillateList(Arrays.asList(genericStacks));
    }

    protected DistillateList(List<GenericStack<DistillateTypes>> delegateIn) {
        super(delegateIn);
    }

    public GenericStack<DistillateTypes> get(DistillateTypes type) {
        return this.stream().filter(stack -> !stack.isEmpty() && (type == DistillateTypes.UNTYPED || stack.getType() == type)).findAny().orElse(GenericStack.empty());
    }

    /**
     * @return the index of a stack of the requested type
     */
    public int indexOf(DistillateTypes type) {
        int index = -1;
        for (int i = 0; i < this.size() && index == -1; i++)
            if ((get(i).isEmpty() && type == DistillateTypes.UNTYPED) || get(i).getType() == type)
                index = i;
        return index;
    }

    public boolean containsType(DistillateTypes type) {
        return this.stream().anyMatch(e -> Objects.equals(e.getType(), type));
    }

    public boolean add(GenericStack<DistillateTypes> stack) {
        for (int i = 0; i < this.size(); i++)
            if (this.get(i).isEmpty()) {
                this.set(i, stack);
                return true;
            }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.stream().allMatch(GenericStack::isEmpty);
    }

    @Override
    public boolean remove(Object arg0) {
        int index = indexOf(arg0);
        if (index == -1)
            return false;
        this.set(index, GenericStack.empty());
        return true;
    }

    @Override
    @Nonnull
    public GenericStack<DistillateTypes> remove(int index) {
        return this.set(index, DistillateStack.EMPTY);
    }

}
