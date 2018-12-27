package ladysnake.dissolution.common.util;

import java.util.HashSet;
import java.util.Set;

public final class CollectionUtil {
    private CollectionUtil() {
        throw new AssertionError("No CollectionUtil for you");
    }

    /**
     * Replays the changes from two sets derived from a common base onto a copy of the latter
     *
     * @param c1       a set derived from <code>diffBase</code>
     * @param c2       a set derived from <code>diffBase</code>
     * @param diffBase the point where the two collections diverged
     * @param <T>      the class of the objects in the collections
     * @return A copy of <code>c1</code> with the changes applied to <code>c2</code> since <code>diffBase</code>
     */
    public static <T> Set<T> mergeHistories(Set<T> c1, Set<T> c2, Set<T> diffBase) {
        Set<T> merged = new HashSet<>(c1);
        // Apply the diff from c2 and diffBase onto merged
        // Step 1: Remove from merged all elements that exist in diffBase but not in c2
        for (T e : diffBase) {
            if (!c2.contains(e)) {
                merged.remove(e);
            }
        }
        // Add to merged all elements that exist in c2 but not in diffBase
        for (T e : c2) {
            if (!diffBase.contains(e)) {
                merged.add(e);
            }
        }
        return merged;
    }
}
