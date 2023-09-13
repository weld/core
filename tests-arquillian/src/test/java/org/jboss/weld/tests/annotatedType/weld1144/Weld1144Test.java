package org.jboss.weld.tests.annotatedType.weld1144;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Extension;
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
 * @author Richard Kennard
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class Weld1144Test {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1144Test.class))
                .addPackage(Weld1144Test.class.getPackage())
                .addAsServiceProvider(Extension.class, CdiExtension.class);
    }

    @Inject
    private Instance<CdiTest2> test;

    @Test
    public void testChildClassFieldIsInjected() {
        CdiTest2 cdiTest2 = test.get();
        assertNotNull(cdiTest2.getSomeInjectedBean2());
    }

    @Test
    public void testSuperclassFieldIsInjected() {
        CdiTest2 cdiTest2 = test.get();
        assertNotNull(cdiTest2.getSomeInjectedBean1());
    }

    @Test
    public void testInitializers(@Original Foo foo, Bar bar) {
        assertTrue(foo.isInitCalled());
        assertFalse(bar.isInitCalled()); // because the initializer is overriden without @Inject
        assertFalse(bar.isSubclassInitCalled());
    }
}
