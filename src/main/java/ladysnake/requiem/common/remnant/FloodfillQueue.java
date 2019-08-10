package ladysnake.requiem.common.remnant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A queue with set properties, optimized for large amounts of data
 * @param <E> the type of elements contained in this queue
 */
public class FloodfillQueue<E> extends AbstractCollection<E> implements Collection<E> {
    /** All elements that should be scanned */
    private Deque<E> toScan = new ArrayDeque<>();
    /** All elements that should be scanned, but in a set (for faster contains checks) */
    private Set<E> toScanSet = new HashSet<>();

    @Override
    public int size() {
        return toScan.size();
    }

    @Override
    public boolean isEmpty() {
        return toScan.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return toScanSet.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Iterator<E> backing = toScan.iterator();
            @Nullable
            private E current;

            @Override
            public boolean hasNext() {
                return backing.hasNext();
            }

            @Override
            public E next() {
                current = backing.next();
                return current;
            }

            @Override
            public void remove() {
                backing.remove();
                toScanSet.remove(current);
            }
        };
    }

    @Override
    public boolean add(E e) {
        if (toScanSet.add(e)) {
            toScan.add(e);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (toScanSet.remove(o)) {
            toScan.remove(o);
            return true;
        }
        return false;
    }

    /**
     * Pops an element from the stack represented by this queue.  In other
     * words, removes and returns the first element of this queue.
     *
     * @return the element at the front of this queue (which is the top
     *         of the stack represented by this queue)
     * @throws NoSuchElementException if this queue is empty
     */
    public E pop() {
        E polled = toScan.pop();
        toScanSet.remove(polled);
        return polled;
    }

    @Override
    public void clear() {
        toScanSet.clear();
        toScan.clear();
    }
}
