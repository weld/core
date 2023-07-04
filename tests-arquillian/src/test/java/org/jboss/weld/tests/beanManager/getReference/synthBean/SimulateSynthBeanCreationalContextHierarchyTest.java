package org.jboss.weld.tests.beanManager.getReference.synthBean;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * NOTE: The functionality this test asserts is not explicitly stated in the spec but it turned out to be relied on in
 * some cases. We therefore want to have a test coverage for it.
 * <p>
 * This test aims to create two synthetic beans and use their creational context to create a link between then so that
 * once one gets destroyed, so should the other.
 */
@RunWith(Arquillian.class)
public class SimulateSynthBeanCreationalContextHierarchyTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SimulateSynthBeanCreationalContextHierarchyTest.class))
                .addPackage(SimulateSynthBeanCreationalContextHierarchyTest.class.getPackage())
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    BeanManager bm;

    @Test
    public void testSimulatingCCHierarchyOnSyntBeans() {
        final Bean<Parent> pb = (Bean<Parent>) bm.resolve(bm.getBeans(Parent.class));
        final CreationalContext<Parent> pcc = bm.createCreationalContext(pb);
        final Parent p = (Parent) bm.getReference(pb, Parent.class, pcc);
        assertTrue(MyExtension.parentCreated);
        assertTrue(MyExtension.childCreated);
        final AlterableContext singletonContext = (AlterableContext) bm.getContext(Singleton.class);
        singletonContext.destroy(pb);
        assertTrue(MyExtension.parentDestroyed);
        assertTrue(MyExtension.childDestroyed);
    }
}
