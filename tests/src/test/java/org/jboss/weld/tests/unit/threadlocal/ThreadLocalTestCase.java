package org.jboss.weld.tests.unit.threadlocal;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.AccessibleObject;

import sun.misc.Unsafe;

public class ThreadLocalTestCase {

    @SuppressWarnings("unused")
    @Inject
    private Foo foo;

    @SuppressWarnings("unused")
    @Inject
    private void someInjectionPointCausingException(Foo foo) {
        throw new RuntimeException();
    }

    @Test
    public void ensureNoThreadLocalLeakOnContexts() throws Exception {
        TestContainer container = new TestContainer(Foo.class, ThreadLocalTestCase.class);
        container.startContainer();
        BeanManager manager = getBeanManager(container);

        Bean<?> testBean = manager.resolve(manager.getBeans(ThreadLocalTestCase.class));

        try {
            manager.getReference(
                testBean,
                ThreadLocalTestCase.class,
                manager.createCreationalContext(testBean));
        } catch (RuntimeException e) {
            // Ignore, expected
        }

        container.stopContainer();
        verifyThreadLocals();
    }

    @Test
    public void ensureNoThreadLocalLeakOnInjectionPoints() throws Exception {
        TestContainer container = new TestContainer(Bar.class, Baz.class);
        container.startContainer();
        BeanManager manager = getBeanManager(container);

        Bean<?> testBean = manager.resolve(manager.getBeans(Baz.class));

        Baz baz = cast(manager.getReference(
            testBean,
            Baz.class,
            manager.createCreationalContext(testBean)));
        baz.getBar().ping();

        container.stopContainer();
        verifyThreadLocals();
    }

    /**
     * Get the bean manager, assuming a flat deployment structure
     */
    public static BeanManager getBeanManager(TestContainer container) {
        return container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());
    }

    private void verifyThreadLocals() throws Exception {
        Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");

        makeAccessible(threadLocalsField);
        Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");

        makeAccessible(inheritableThreadLocalsField);

        Thread thread = Thread.currentThread();

        Class<?> tlmClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
        Field size = tlmClass.getDeclaredField("size");
        Field table = tlmClass.getDeclaredField("table");

        makeAccessible(size);
        makeAccessible(table);

        verifyThreadLocalValues(
            extractThreadLocalValues(
                threadLocalsField.get(thread), table));

        verifyThreadLocalValues(
            extractThreadLocalValues(
                inheritableThreadLocalsField.get(thread), table));

    }

    private void verifyThreadLocalValues(Map<Object, Object> values) {
        for (Map.Entry<Object, Object> entry : values.entrySet()) {
            String keyName = String.valueOf(entry.getKey());
            if (keyName != null) {
                Assert.assertFalse(
                    keyName.startsWith("org.jboss.weld"),
                    "ThreadLocal variable with key [" + keyName + "] with value[" + entry.getValue() + "] found");
            }
        }
    }

    private Map<Object, Object> extractThreadLocalValues(Object map, Field internalTableField) throws NoSuchMethodException,
        IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        Map<Object, Object> values = new HashMap<Object, Object>();
        if (map != null) {
            Method mapRemove = map.getClass().getDeclaredMethod("remove", new Class[] { ThreadLocal.class });

            makeAccessible(mapRemove);
            Object[] table = (Object[]) (Object[]) internalTableField.get(map);
            if (table != null) {
                for (Object aTable : table) {
                    if (aTable != null) {
                        Object key = ((Reference<?>) aTable).get();
                        Field valueField = aTable.getClass().getDeclaredField("value");

                        makeAccessible(valueField);
                        Object value = valueField.get(aTable);
                        values.put(key, value);
                    }
                }
            }
        }
        return values;
    }

    /**
     * JDK 9+ doesn't allow us to call setAccessible() directly, hence we hack it with Unsafe 
     */
    private void makeAccessible(AccessibleObject ao) throws NoSuchFieldException, IllegalAccessException {
        // get Unsafe singleton instance
        Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
        singleoneInstanceField.setAccessible(true);
        Unsafe theUnsafe = (Unsafe) singleoneInstanceField.get(null);

        // get the offset of the override field in AccessibleObject
        long overrideOffset = theUnsafe.objectFieldOffset(AccessibleObject.class.getDeclaredField("override"));

        // make both accessible
        theUnsafe.putBoolean(ao, overrideOffset, true);
        theUnsafe.putBoolean(ao, overrideOffset, true);
    }
}
