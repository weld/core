package org.jboss.weld.tests.classDefining.unimplemented.interfaces;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.LocalInterface1;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.LocalInterface2;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.NotImplementedButDeclaredInterface;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.impl.StatelessLocalBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class ProxyCreationForEjbLocalTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyCreationForEjbLocalTest.class))
                .addClasses(ProxyCreationForEjbLocalTest.class, LocalInterface1.class, LocalInterface2.class,
                        NotImplementedButDeclaredInterface.class, StatelessLocalBean.class);
    }

    @Inject
    StatelessLocalBean bean1;

    @Inject
    NotImplementedButDeclaredInterface bean2;

    @Test
    public void testProxyPackageMatchesTheClass() {
        // sanity check of the testing setup
        Assert.assertEquals(LocalInterface1.class.getSimpleName(), bean1.ping1());

        // also assert invoking the method from the interface bean doesn't implement directly
        Assert.assertEquals(NotImplementedButDeclaredInterface.class.getSimpleName(), bean2.ping3());

        // The assertion is based solely on inspecting the proxy format - expected package and first mentioned class
        // We cannot rely on verifying that the class can be defined because this runs on WFLY which directly uses
        // ClassLoader#defineClass in which case it's a non-issue. The mismatch only shows when using MethodHandles.Lookup
        // see https://github.com/jakartaee/platform-tck/issues/1194 for more information
        Assert.assertEquals(StatelessLocalBean.class.getPackage(), bean1.getClass().getPackage());
        Assert.assertTrue(bean1.getClass().getName().startsWith(StatelessLocalBean.class.getName()));
    }
}
