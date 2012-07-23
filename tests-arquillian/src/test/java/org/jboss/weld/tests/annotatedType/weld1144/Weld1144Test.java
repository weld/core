package org.jboss.weld.tests.annotatedType.weld1144;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Richard Kennard
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class Weld1144Test {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(Weld1144Test.class.getPackage())
                .addAsServiceProvider(Extension.class, CdiExtension.class);
    }

    @Inject
    private Instance<CdiTest2> test;

    @Test
    @Ignore
    public void testChildClassFieldIsInjected() {
        CdiTest2 cdiTest2 = test.get();
        assertNotNull(cdiTest2.getSomeInjectedBean2());
    }

    @Test
    @Ignore
    public void testSuperclassFieldIsInjected() {
        CdiTest2 cdiTest2 = test.get();
        assertNotNull(cdiTest2.getSomeInjectedBean1());
    }

    @Test
    public void testDummy() {
        // remove it
    }
}
