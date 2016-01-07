package org.jboss.weld.tests.injectionPoint.weld1177;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class Weld1177Test {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1177Test.class))
                .addPackage(Weld1177Test.class.getPackage())
                .addClass(Utils.class);
    }

    @Inject
    private Foo foo;

    @Inject
    private Foo foo2;

    @Inject
    private Bar bar;

    @Inject
    private Baz baz;

    @Inject
    private Instance<Foo> fooInstance;

    @Inject
    private Instance<Foo> fooInstance2;

    @Test
    public void testInjectionPointInManagedBean() throws Exception {
        assertNotNull(bar.getInjectionPoint());
        assertEquals(Weld1177Test.class.getDeclaredField("bar"), bar.getInjectionPointMember());
        assertEquals(Bar.class, bar.getInjectionPointType());
    }

    @Test
    public void testInjectionPointInSLSB() throws Exception {
        assertEquals(Weld1177Test.class.getDeclaredField("foo"), foo.getInjectionPointMember());
        assertEquals(Weld1177Test.class.getDeclaredField("foo2"), foo2.getInjectionPointMember());
        assertEquals(Weld1177Test.class.getDeclaredField("baz"), baz.getInjectionPointMember());
        assertEquals(Foo.class, foo.getInjectionPointType());
        assertEquals(Foo.class, foo2.getInjectionPointType());
        assertEquals(Baz.class, baz.getInjectionPointType());

        assertEquals(Foo.class.getDeclaredField("bar"), foo.getBarInjectionPointMember());
        assertEquals(Foo.class.getDeclaredField("bar"), foo2.getBarInjectionPointMember());
    }

    @Test
    public void testInjectionPointInSLSBWithInstance() throws Exception {
        Foo foo = fooInstance.get();
        Foo foo2 = fooInstance2.get();
        foo.doSomething();
        foo2.doSomething();
        assertNotNull(foo.getInjectionPoint());
        assertNotNull(foo2.getInjectionPoint());
        assertEquals(Weld1177Test.class.getDeclaredField("fooInstance"), foo.getInjectionPointMember());
        assertEquals(Weld1177Test.class.getDeclaredField("fooInstance2"), foo2.getInjectionPointMember());
        assertEquals(Foo.class, foo.getInjectionPointType());
        assertEquals(Foo.class, foo2.getInjectionPointType());
    }

    @Test(expected = IllegalStateException.class)
    public void testInjectionPointOutsideSLSB() throws Exception {
        assertNotNull(foo.getInjectionPoint());
        // This should yield an exception - injection point metadata injected into a stateless session bean may only be accessed within its business method
        // invocation
        foo.getInjectionPoint().getType();
    }

}
