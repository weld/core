package org.jboss.weld.tests.unit.util.collections;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.util.collections.ArraySet;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ArraySetTest {

    @Test
    public void testEqualsAndHashCode() {
        Set<String> arraySet = new ArraySet<>("foo", "bar", "A", "B");
        Set<String> hashSet = new HashSet<>();
        hashSet.add("B");
        hashSet.add("bar");
        hashSet.add("foo");
        hashSet.add("A");
        assertEquals(arraySet, hashSet);
        assertEquals(arraySet.hashCode(), hashSet.hashCode());
    }

}
