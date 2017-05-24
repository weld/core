package org.jboss.weld.tests.injectionPoint.weld1177;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
public class EnterpriseInjectionPointMetadataTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EnterpriseInjectionPointMetadataTest.class))
                .addPackage(EnterpriseInjectionPointMetadataTest.class.getPackage())
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
    private Qux qux;

    @Inject
    private Corge corge;

    @Inject
    private Instance<Foo> fooInstance;

    @Inject
    private Instance<Foo> fooInstance2;

    @Test
    public void testInjectionPointInManagedBean() throws Exception {
        assertNotNull(bar.getInjectionPoint());
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("bar"), bar.getInjectionPointMember());
        assertEquals(Bar.class, bar.getInjectionPointType());
    }

    @Test
    public void testInjectionPointInSLSB() throws Exception {
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("foo"), foo.getInjectionPointMember());
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("foo2"), foo2.getInjectionPointMember());
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("baz"), baz.getInjectionPointMember());
        assertEquals(Foo.class, foo.getInjectionPointType());
        assertEquals(Foo.class, foo2.getInjectionPointType());
        assertEquals(Baz.class, baz.getInjectionPointType());

        assertEquals(Foo.class.getDeclaredField("bar"), foo.getBarInjectionPointMember());
        assertEquals(Foo.class.getDeclaredField("bar"), foo2.getBarInjectionPointMember());
        assertBeanIsNotNull(Baz.class, baz.getGarply().getInjectionPoint().getBean());
    }

    @Test
    public void testInjectionPointMetadataInSFSB() throws Exception {
        assertBeanIsNotNull(Qux.class, qux.getGarply().getInjectionPoint().getBean());
    }

    @Test
    public void testInjectionPointMetadataInSingletonSB() throws Exception {
        assertBeanIsNotNull(Corge.class, corge.getGarply().getInjectionPoint().getBean());
    }

    @Test
    public void testInjectionPointMetadataInNonContextualEJB() throws NamingException {
        Baz baz = (Baz) new InitialContext().lookup("java:module/Baz");
        assertBeanIsNull(baz.getClass(), baz.getGarply().getInjectionPoint().getBean());

        Corge corge = (Corge) new InitialContext().lookup("java:module/Corge");
        assertBeanIsNull(corge.getClass(), corge.getGarply().getInjectionPoint().getBean());

        Qux qux = (Qux) new InitialContext().lookup("java:module/Qux");
        assertBeanIsNull(qux.getClass(), qux.getGarply().getInjectionPoint().getBean());
    }

    @Test
    public void testInjectionPointInSLSBWithInstance() throws Exception {
        Foo foo = fooInstance.get();
        Foo foo2 = fooInstance2.get();
        foo.doSomething();
        foo2.doSomething();
        assertNotNull(foo.getInjectionPoint());
        assertNotNull(foo2.getInjectionPoint());
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("fooInstance"), foo.getInjectionPointMember());
        assertEquals(EnterpriseInjectionPointMetadataTest.class.getDeclaredField("fooInstance2"), foo2.getInjectionPointMember());
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

    private void assertBeanIsNull(Class<?> clazz, Bean<?> bean) {
        assertNull("InjectionPoint Bean metadata was not null in " + clazz, bean);
    }

    private void assertBeanIsNotNull(Class<?> clazz, Bean<?> bean) {
        assertNotNull("InjectionPoint Bean metadata was null in " + clazz, bean);
    }

}
