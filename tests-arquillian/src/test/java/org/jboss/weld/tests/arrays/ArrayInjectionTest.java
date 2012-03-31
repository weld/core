package org.jboss.weld.tests.arrays;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Instance;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class ArrayInjectionTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class)
            .addClasses(Foo.class, Bar.class, ArrayProducer.class);
    }

    @Test
    public void testDirectArrayInjection(int[] intArray, Foo[] fooArray) {
        assertNotNull(intArray);
        assertNotNull(fooArray);
    }

    @Test
    public void testIntArrayInstanceInjection(Instance<int[]> intArrayInstance) {
        assertNotNull(intArrayInstance.get());
    }

    @Test
    public void testFooArrayInstanceInjection(Instance<Foo[]> fooArrayInstance) {
        assertNotNull(fooArrayInstance.get());
    }

    @Test
    public void testStringBarArrayInstanceInjection(Instance<Bar<String>[]> stringBarArrayInstance) {
        Bar<String>[] stringBarArray = stringBarArrayInstance.get();
        assertArrayEquals(ArrayProducer.STRING_BAR_ARRAY, stringBarArray);
    }

    @Test
    public void testStringBarArrayInjection(Bar<String>[] stringBarArray) {
        assertArrayEquals(ArrayProducer.STRING_BAR_ARRAY, stringBarArray);
    }

    @Test
    public void testIntegerBarArrayInjection(Bar<Integer>[] integerBarArray) {
        assertArrayEquals(ArrayProducer.INTEGER_BAR_ARRAY, integerBarArray);
    }

    @Test
    public void testStringBarInstanceInjection(Instance<Bar<String>> stringBarInstance) {
        Bar<String> stringBar = stringBarInstance.get();
        assertEquals(ArrayProducer.STRING_BAR, stringBar);
    }

    @Test
    public void testIntegerBarInstanceInjection(Instance<Bar<Integer>> integerBarInstance) {
        Bar<Integer> integerBar = integerBarInstance.get();
        assertEquals(ArrayProducer.INTEGER_BAR, integerBar);
    }

}