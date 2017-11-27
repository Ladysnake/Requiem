package ladysnake.dissolution.api;

import jline.internal.Nullable;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenericStackList<T> extends NonNullList<GenericStack<T>> {

    protected final GenericStack<T> defaultElement;

    @SuppressWarnings("unchecked")
    public static <T> GenericStackList<T> withSize(int size) {
        GenericStack<T>[] genericStacks = new GenericStack[size];
        Arrays.fill(genericStacks, GenericStack.empty());
        return new GenericStackList<>(Arrays.asList(genericStacks));
    }

    protected GenericStackList(List<GenericStack<T>> delegateIn) {
        this(delegateIn, GenericStack.empty());
    }

    protected GenericStackList(List<GenericStack<T>> delegateIn, GenericStack<T> defaultElement) {
        super(delegateIn, defaultElement);
        this.defaultElement = defaultElement;
    }

    public GenericStack<T> get(@Nullable T type) {
        return this.stream().filter(stack -> !stack.isEmpty() && (type == null || type.equals(stack.getType()))).findAny().orElse(defaultElement);
    }

    /**
     * Searches for a stack that has a type equal to the one specified.
     *
     * @return the index of a stack of the requested type
     */
    public int indexOfType(@Nullable T type) {
        int index = -1;
        for (int i = 0; i < this.size() && index == -1; i++)
            if ((get(i).isEmpty() && type == null) || get(i).getType() == type)
                index = i;
        return index;
    }

    public boolean containsType(@Nullable T type) {
        return this.stream().anyMatch(e -> Objects.equals(e.getType(), type));
    }

    public boolean add(GenericStack<T> stack) {
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
        this.set(index, defaultElement);
        return true;
    }

    @Override
    @Nonnull
    public GenericStack<T> remove(int index) {
        return this.set(index, defaultElement);
    }

}
