package org.jboss.weld.tests.arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class ArrayInjectionTest {

    @Inject
    int[] intArray;

    @Inject
    Foo[] fooArray;

    @Inject
    Instance<int[]> intArrayInstance;

    @Inject
    Instance<Foo[]> fooArrayInstance;

    @Inject
    Instance<Bar<String>[]> stringBarArrayInstance;

    @Inject
    Bar<String>[] stringBarArray;

    @Inject
    Bar<Integer>[] integerBarArray;

    @Inject
    Instance<Bar<String>> stringBarInstance;

    @Inject
    Instance<Bar<Integer>> integerBarInstance;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ArrayInjectionTest.class))
                .addClasses(Foo.class, Bar.class, ArrayProducer.class);
    }

    @Test
    public void testDirectArrayInjection() {
        assertNotNull(intArray);
        assertNotNull(fooArray);
    }

    @Test
    public void testIntArrayInstanceInjection() {
        assertNotNull(intArrayInstance.get());
    }

    @Test
    public void testFooArrayInstanceInjection() {
        assertNotNull(fooArrayInstance.get());
    }

    @Test
    public void testStringBarArrayInstanceInjection() {
        Bar<String>[] stringBarArray = stringBarArrayInstance.get();
        assertArrayEquals(ArrayProducer.STRING_BAR_ARRAY, stringBarArray);
    }

    @Test
    public void testStringBarArrayInjection() {
        assertArrayEquals(ArrayProducer.STRING_BAR_ARRAY, stringBarArray);
    }

    @Test
    public void testIntegerBarArrayInjection() {
        assertArrayEquals(ArrayProducer.INTEGER_BAR_ARRAY, integerBarArray);
    }

    @Test
    public void testStringBarInstanceInjection() {
        Bar<String> stringBar = stringBarInstance.get();
        assertEquals(ArrayProducer.STRING_BAR, stringBar);
    }

    @Test
    public void testIntegerBarInstanceInjection() {
        Bar<Integer> integerBar = integerBarInstance.get();
        assertEquals(ArrayProducer.INTEGER_BAR, integerBar);
    }

}
