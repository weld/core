package org.jboss.weld.tests.injectionPoint.notInjectedInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
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
 * Tests InjectionPoint metadata when Instance is not injected but obtained
 * programmatically via BeanContainer.createInstance().
 *
 * Per CDI 5.0 spec clarification (issue #779, PR #931):
 * - getBean() returns null
 * - getMember() returns null
 * - getAnnotated() returns null
 * - getType() returns the required type from Instance.select()
 * - getQualifiers() returns the required qualifiers from Instance.select()
 * - isDelegate() returns false
 * - isTransient() returns false
 */
@RunWith(Arquillian.class)
public class NotInjectedInstanceInjectionPointTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(NotInjectedInstanceInjectionPointTest.class))
                .addPackage(NotInjectedInstanceInjectionPointTest.class.getPackage());
    }

    @Inject
    BeanContainer container;

    @Test
    public void testGetBeanReturnsNull() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertNull("getBean() should return null for non-injected Instance", foo.getInjectionPoint().getBean());
    }

    @Test
    public void testGetMemberReturnsNull() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertNull("getMember() should return null for non-injected Instance", foo.getInjectionPoint().getMember());
    }

    @Test
    public void testGetAnnotatedReturnsNull() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertNull("getAnnotated() should return null for non-injected Instance", foo.getInjectionPoint().getAnnotated());
    }

    @Test
    public void testIsDelegateReturnsFalse() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertFalse("isDelegate() should return false for non-injected Instance", foo.getInjectionPoint().isDelegate());
    }

    @Test
    public void testIsTransientReturnsFalse() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertFalse("isTransient() should return false for non-injected Instance", foo.getInjectionPoint().isTransient());
    }

    @Test
    public void testGetTypeReturnsRequiredType() {
        Foo foo = container.createInstance().select(Foo.class).get();
        assertEquals("getType() should return Foo.class", Foo.class, foo.getInjectionPoint().getType());
    }

    @Test
    public void testGetTypeReturnsSubtype() {
        Foo foo = container.createInstance().select(NiceFoo.class, Any.Literal.INSTANCE).get();
        assertEquals("getType() should return NiceFoo.class when selected by subtype",
                NiceFoo.class, foo.getInjectionPoint().getType());
    }

    @Test
    public void testGetTypeWhenSelectedByQualifier() {
        Foo foo = container.createInstance().select(Foo.class, new Nice.Literal()).get();
        assertEquals("getType() should return Foo.class when selected by qualifier",
                Foo.class, foo.getInjectionPoint().getType());
    }

    @Test
    public void testGetQualifiersDefault() {
        Foo foo = container.createInstance().select(Foo.class).get();
        Set<Annotation> qualifiers = foo.getInjectionPoint().getQualifiers();
        assertTrue("qualifiers should contain @Default", qualifiers.contains(Default.Literal.INSTANCE));
    }

    @Test
    public void testGetQualifiersWithExplicitQualifier() {
        Foo foo = container.createInstance().select(Foo.class, new Nice.Literal()).get();
        Set<Annotation> qualifiers = foo.getInjectionPoint().getQualifiers();
        assertTrue("qualifiers should contain @Nice", qualifiers.stream()
                .anyMatch(a -> a.annotationType().equals(Nice.class)));
    }

    @Test
    public void testGetQualifiersWithSubtypeSelection() {
        Foo foo = container.createInstance().select(NiceFoo.class, Any.Literal.INSTANCE).get();
        Set<Annotation> qualifiers = foo.getInjectionPoint().getQualifiers();
        assertTrue("qualifiers should contain @Any", qualifiers.contains(Any.Literal.INSTANCE));
    }
}
