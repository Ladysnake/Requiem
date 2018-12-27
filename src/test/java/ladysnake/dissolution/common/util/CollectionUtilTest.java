package ladysnake.dissolution.common.util;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CollectionUtilTest {

    @Test
    public void mergeHistories() {
        Set<Integer> base = Sets.newHashSet(1, 2, 3, 4, 5, 6);
        Set<Integer> c1 = new HashSet<>(base);
        Set<Integer> c2 = new HashSet<>(base);
        c1.remove(1);
        c1.add(7);
        c2.add(8);
        c2.remove(4);
        Collection<Integer> oracle = Sets.newHashSet(2, 3, 5, 6, 7, 8);
        Collection<Integer> result = CollectionUtil.mergeHistories(c1, c2, base);
        assertEquals(oracle, result);
    }

    @Test
    public void mergeHistoriesNoHistory() {
        Set<Double> base = new HashSet<>();
        Set<Double> c1 = new HashSet<>();
        Set<Double> c2 = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            c1.add(Math.random());
            c2.add(Math.random());
        }
        Set<Double> oracle = new HashSet<>(c1);
        oracle.addAll(c2);
        Set<Double> result = CollectionUtil.mergeHistories(c1, c2, base);
        assertEquals(oracle, result);
    }
}
